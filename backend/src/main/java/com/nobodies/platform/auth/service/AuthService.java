package com.nobodies.platform.auth.service;

import com.nobodies.platform.auth.dto.AuthResponse;
import com.nobodies.platform.auth.dto.LoginRequest;
import com.nobodies.platform.auth.dto.PasswordResetConfirmRequest;
import com.nobodies.platform.auth.dto.PasswordResetRequest;
import com.nobodies.platform.auth.dto.RegisterRequest;
import com.nobodies.platform.common.enums.RoleType;
import com.nobodies.platform.common.exception.BusinessException;
import com.nobodies.platform.common.util.JwtUtil;
import com.nobodies.platform.common.util.TokenHashUtil;
import com.nobodies.platform.mail.service.MailService;
import com.nobodies.platform.user.entity.PasswordResetToken;
import com.nobodies.platform.user.entity.User;
import com.nobodies.platform.user.repository.PasswordResetTokenRepository;
import com.nobodies.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private MailService mailService;

    @Value("${auth.password-reset.token-expiration-minutes}")
    private long passwordResetTokenExpirationMinutes;

    @Value("${app.frontend.reset-password-url}")
    private String resetPasswordUrl;

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(RoleType.UPLOADER.name());
        user.setStatus("ACTIVE");
        user.setTokenVersion(0L);
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole(), user.getTokenVersion());
        return new AuthResponse(token, jwtUtil.getExpirationSeconds(), user.getRole());
    }

    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserId(user.getId());

            String rawToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
            PasswordResetToken passwordResetToken = new PasswordResetToken();
            passwordResetToken.setUserId(user.getId());
            passwordResetToken.setTokenHash(TokenHashUtil.sha256(rawToken));
            passwordResetToken.setExpiresAt(LocalDateTime.now().plusMinutes(passwordResetTokenExpirationMinutes));
            passwordResetTokenRepository.save(passwordResetToken);

            String subject = "Reset your Nobodies password";
            String content = String.format(
                "We received a request to reset your Nobodies account password.%n%nReset link:%n%s%n%nThis link expires in %d minutes. If you did not request this, you can safely ignore this email.",
                buildResetPasswordLink(rawToken),
                passwordResetTokenExpirationMinutes
            );
            mailService.send(user.getEmail(), subject, content);
        });
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByTokenHash(TokenHashUtil.sha256(request.getToken()))
            .orElseThrow(() -> new BusinessException("Reset token is invalid or expired"));

        if (passwordResetToken.getUsedAt() != null || passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Reset token is invalid or expired");
        }

        User user = userRepository.findById(passwordResetToken.getUserId())
            .orElseThrow(() -> new BusinessException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        passwordResetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(passwordResetToken);
        passwordResetTokenRepository.deleteByUserId(user.getId());
    }

    public void logout(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("User not found"));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    private String buildResetPasswordLink(String token) {
        String delimiter = resetPasswordUrl.contains("?") ? "&" : "?";
        return resetPasswordUrl + delimiter + "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }
}
