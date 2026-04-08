package com.nobodies.platform.upload.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadChunkStatusResponse {
    private Integer chunkNumber;
    private String status;
    private Long chunkSize;
    private Integer retryCount;
    private String etag;
    private String uploadedAt;
    private Boolean uploaded;
}
