package com.nobodies.platform.user.service;

import com.nobodies.platform.common.enums.RoleType;
import com.nobodies.platform.common.exception.BusinessException;
import com.nobodies.platform.user.dto.UpdateRoleRequest;
import com.nobodies.platform.user.dto.UserResponse;
import com.nobodies.platform.user.entity.User;
import com.nobodies.platform.user.repository.UserRepository;
import com.nobodies.platform.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
            .map(user -> UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .storageUsedBytes(videoRepository.sumFileSizeByUploaderId(user.getId()))
                .videoCount(videoRepository.countByUploaderIdAndDeletedFalse(user.getId()))
                .build())
            .toList();
    }

    public void updateRole(Long id, UpdateRoleRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new BusinessException("User not found"));
        try {
            RoleType.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Invalid role");
        }
        user.setRole(request.getRole().toUpperCase());
        userRepository.save(user);
    }
}
