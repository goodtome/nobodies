package com.nobodies.platform.video.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoDetailResponse {
    private Long id;
    private String title;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private String status;
    private String playbackUrl;
}
