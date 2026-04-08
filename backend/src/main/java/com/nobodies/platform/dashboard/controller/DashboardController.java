package com.nobodies.platform.dashboard.controller;

import com.nobodies.platform.common.api.ApiResponse;
import com.nobodies.platform.common.enums.RoleType;
import com.nobodies.platform.common.util.SecurityUtil;
import com.nobodies.platform.dashboard.dto.DashboardSummaryResponse;
import com.nobodies.platform.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final VideoService videoService;

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> summary() {
        String role = SecurityUtil.getCurrentUserRole();
        DashboardSummaryResponse response = DashboardSummaryResponse.builder()
            .role(role)
            .videoCount(videoService.getCurrentUserVideoCount())
            .storageUsedBytes(videoService.getCurrentUserStorageUsed())
            .totalVideoCount(RoleType.ADMIN.name().equals(role) ? videoService.getTotalVideoCount() : null)
            .totalStorageUsedBytes(RoleType.ADMIN.name().equals(role) ? videoService.getTotalStorageUsed() : null)
            .build();
        return ApiResponse.ok(response);
    }
}
