package com.nobodies.platform.auth.service;

import com.nobodies.platform.auth.dto.PasswordResetConfirmRequest;
import com.nobodies.platform.auth.dto.PasswordResetRequest;
import com.nobodies.platform.common.exception.BusinessException;
import com.nobodies.platform.common.util.TokenHashUtil;
import com.nobodies.platform.mail.service.MailService;
import com.nobodies.platform.user.entity.PasswordResetToken;
import com.nobodies.platform.user.entity.User;
import com.nobodies.platform.user.repository.PasswordResetTokenRepository;
import com.nobodies.platform.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.nobodies.platform.common.util.JwtUtil jwtUtil;

    @Mock
    private MailService mailService;

    @InjectMocks
    private AuthService authService;

    @Test
    void requestPasswordResetShouldCreateTokenAndSendEmail() {
        User user = buildUser();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        ReflectionTestUtils.setField(authService, "passwordResetTokenExpirationMinutes", 30L);
        ReflectionTestUtils.setField(authService, "resetPasswordUrl", "http://localhost:5173/reset-password");

        authService.requestPasswordReset(buildRequest());

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).deleteByUserId(1L);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        verify(mailService).send(org.mockito.ArgumentMatchers.eq("user@example.com"), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.contains("http://localhost:5173/reset-password?token="));
        assertEquals(1L, tokenCaptor.getValue().getUserId());
        assertEquals(64, tokenCaptor.getValue().getTokenHash().length());
        assertTrue(tokenCaptor.getValue().getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(29)));
    }

    @Test
    void confirmPasswordResetShouldRejectUnknownToken() {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        request.setToken("invalid-token");
        request.setPassword("Password123");
        when(passwordResetTokenRepository.findByTokenHash(TokenHashUtil.sha256("invalid-token"))).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.confirmPasswordReset(request));

        assertEquals("Reset token is invalid or expired", exception.getMessage());
    }

    @Test
    void confirmPasswordResetShouldUpdatePasswordAndInvalidateExistingSessions() {
        User user = buildUser();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setId(10L);
        resetToken.setUserId(1L);
        resetToken.setTokenHash(TokenHashUtil.sha256("valid-token"));
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        request.setToken("valid-token");
        request.setPassword("NewPassword123");

        when(passwordResetTokenRepository.findByTokenHash(TokenHashUtil.sha256("valid-token"))).thenReturn(Optional.of(resetToken));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPassword123")).thenReturn("encoded-password");

        authService.confirmPasswordReset(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(userRepository).save(userCaptor.capture());
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        verify(passwordResetTokenRepository).deleteByUserId(1L);
        assertNotEquals("old-hash", userCaptor.getValue().getPasswordHash());
        assertEquals("encoded-password", userCaptor.getValue().getPasswordHash());
        assertEquals(1L, userCaptor.getValue().getTokenVersion());
        assertTrue(tokenCaptor.getValue().getUsedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    private PasswordResetRequest buildRequest() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("user@example.com");
        return request;
    }

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPasswordHash("old-hash");
        user.setRole("UPLOADER");
        user.setStatus("ACTIVE");
        user.setTokenVersion(0L);
        return user;
    }
}
