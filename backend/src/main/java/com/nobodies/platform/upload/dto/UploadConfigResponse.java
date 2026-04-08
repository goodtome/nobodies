package com.nobodies.platform.upload.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UploadConfigResponse {
    private Long maxFileSize;
    private List<String> allowedTypes;
}
