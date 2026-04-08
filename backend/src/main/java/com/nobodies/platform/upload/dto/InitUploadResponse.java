package com.nobodies.platform.upload.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InitUploadResponse {
    private Long uploadSessionId;
    private String objectKey;
    private String ossUploadId;
    private Integer totalChunks;
    private Integer chunkRetryTimes;
    private Integer uploadedChunksCount;
    private Integer pendingChunksCount;
    private Integer abortedChunksCount;
    private List<Integer> uploadedChunks;
    private List<UploadChunkStatusResponse> chunkStatuses;
}
