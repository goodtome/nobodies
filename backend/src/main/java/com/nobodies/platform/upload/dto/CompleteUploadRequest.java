package com.nobodies.platform.upload.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompleteUploadRequest {
    @NotBlank(message = "Title is required")
    private String title;
}
