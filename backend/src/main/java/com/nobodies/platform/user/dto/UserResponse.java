package com.nobodies.platform.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String role;
    private String status;
    private Long storageUsedBytes;
    private Long videoCount;
}
