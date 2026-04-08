package com.nobodies.platform.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardSummaryResponse {
    private String role;
    private Long videoCount;
    private Long storageUsedBytes;
    private Long totalVideoCount;
    private Long totalStorageUsedBytes;
}
