package com.nobodies.platform.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadChunkResponse {
    private Long sessionId;
    private Integer chunkNumber;
    private String status;
    private String etag;
}
