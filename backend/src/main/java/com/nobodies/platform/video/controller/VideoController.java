package com.nobodies.platform.video.controller;

import com.nobodies.platform.common.api.ApiResponse;
import com.nobodies.platform.video.dto.VideoDetailResponse;
import com.nobodies.platform.video.dto.VideoResponse;
import com.nobodies.platform.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @GetMapping
    public ApiResponse<List<VideoResponse>> list() {
        return ApiResponse.ok(videoService.listVideosForCurrentUser());
    }

    @GetMapping("/{id}")
    public ApiResponse<VideoDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(videoService.getVideoDetail(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('UPLOADER','ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        videoService.softDelete(id);
        return ApiResponse.ok(null);
    }
}
