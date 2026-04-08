package com.nobodies.platform.upload.controller;

import com.nobodies.platform.common.api.ApiResponse;
import com.nobodies.platform.upload.dto.CompleteUploadRequest;
import com.nobodies.platform.upload.dto.InitUploadRequest;
import com.nobodies.platform.upload.dto.InitUploadResponse;
import com.nobodies.platform.upload.dto.UploadChunkResponse;
import com.nobodies.platform.upload.dto.UploadConfigResponse;
import com.nobodies.platform.upload.dto.UploadProgressResponse;
import com.nobodies.platform.upload.service.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/videos/upload")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('UPLOADER','ADMIN')")
public class UploadController {

    private final UploadService uploadService;

    @GetMapping("/config")
    public ApiResponse<UploadConfigResponse> getUploadConfig() {
        return ApiResponse.ok(uploadService.getUploadConfig());
    }

    @PostMapping("/init")
    public ApiResponse<InitUploadResponse> initUpload(@Valid @RequestBody InitUploadRequest request) {
        return ApiResponse.ok(uploadService.initUpload(request));
    }

    @PutMapping(path = "/{id}/chunk", consumes = {"multipart/form-data"})
    public ApiResponse<UploadChunkResponse> uploadChunk(@PathVariable Long id,
                                                        @RequestParam Integer chunkNumber,
                                                        @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(uploadService.uploadChunk(id, chunkNumber, file));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<Void> completeUpload(@PathVariable Long id, @Valid @RequestBody CompleteUploadRequest request) {
        uploadService.completeUpload(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}/abort")
    public ApiResponse<Void> abortUpload(@PathVariable Long id) {
        uploadService.abortUpload(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/progress")
    public ApiResponse<UploadProgressResponse> getProgress(@PathVariable Long id) {
        return ApiResponse.ok(uploadService.getProgress(id));
    }
}
