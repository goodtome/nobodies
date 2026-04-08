package com.nobodies.platform.upload.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UploadProgressResponse {
    private Long sessionId;
    private String status;
    private Integer totalChunks;
    private Integer uploadedChunksCount;
    private Integer pendingChunksCount;
    private Integer abortedChunksCount;
    private List<Integer> uploadedChunks;
    private List<UploadChunkStatusResponse> chunkStatuses;
}
