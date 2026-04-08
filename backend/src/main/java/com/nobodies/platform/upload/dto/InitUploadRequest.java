package com.nobodies.platform.upload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InitUploadRequest {
    @NotBlank(message = "File name is required")
    private String fileName;

    @NotNull(message = "File size is required")
    private Long fileSize;

    @NotBlank(message = "File type is required")
    private String fileType;

    @NotNull(message = "Chunk size is required")
    private Long chunkSize;
}
