package com.nobodies.platform.video.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoResponse {
    private Long id;
    private String title;
    private String fileName;
    private Long fileSize;
    private String status;
}
