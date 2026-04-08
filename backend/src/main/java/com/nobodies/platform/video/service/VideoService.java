package com.nobodies.platform.video.service;

import com.nobodies.platform.common.enums.RoleType;
import com.nobodies.platform.common.enums.VideoStatus;
import com.nobodies.platform.common.exception.BusinessException;
import com.nobodies.platform.common.util.SecurityUtil;
import com.nobodies.platform.storage.OssMultipartService;
import com.nobodies.platform.video.dto.VideoDetailResponse;
import com.nobodies.platform.video.dto.VideoResponse;
import com.nobodies.platform.video.entity.Video;
import com.nobodies.platform.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final OssMultipartService ossMultipartService;

    @Value("${aliyun.oss.presign-duration-minutes}")
    private long presignDurationMinutes;

    public List<VideoResponse> listVideosForCurrentUser() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();
        List<Video> videos = RoleType.ADMIN.name().equals(role) || RoleType.VIEWER.name().equals(role)
            ? videoRepository.findByDeletedFalse()
            : videoRepository.findByUploaderIdAndDeletedFalse(currentUserId);

        return videos.stream()
            .map(video -> VideoResponse.builder()
                .id(video.getId())
                .title(video.getTitle())
                .fileName(video.getFileName())
                .fileSize(video.getFileSize())
                .status(video.getStatus())
                .build())
            .toList();
    }

    public VideoDetailResponse getVideoDetail(Long id) {
        Video video = videoRepository.findById(id)
            .filter(item -> !item.isDeleted())
            .orElseThrow(() -> new BusinessException("Video not found"));
        validateReadAccess(video);

        return VideoDetailResponse.builder()
            .id(video.getId())
            .title(video.getTitle())
            .fileName(video.getFileName())
            .fileSize(video.getFileSize())
            .fileType(video.getFileType())
            .status(video.getStatus())
            .playbackUrl(ossMultipartService.generatePresignedUrl(video.getOssKey(), Duration.ofMinutes(presignDurationMinutes)).toString())
            .build();
    }

    public void softDelete(Long id) {
        Video video = videoRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Video not found"));
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();
        boolean canDelete = RoleType.ADMIN.name().equals(role) || video.getUploaderId().equals(currentUserId);
        if (!canDelete) {
            throw new BusinessException("No permission to delete this video");
        }
        video.setDeleted(true);
        video.setStatus(VideoStatus.DELETED.name());
        videoRepository.save(video);
    }

    public long getCurrentUserStorageUsed() {
        return videoRepository.sumFileSizeByUploaderId(SecurityUtil.getCurrentUserId());
    }

    public long getCurrentUserVideoCount() {
        return videoRepository.countByUploaderIdAndDeletedFalse(SecurityUtil.getCurrentUserId());
    }

    public long getTotalStorageUsed() {
        return videoRepository.sumAllFileSize();
    }

    public long getTotalVideoCount() {
        return videoRepository.countByDeletedFalse();
    }

    private void validateReadAccess(Video video) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();
        boolean canRead = RoleType.ADMIN.name().equals(role)
            || RoleType.VIEWER.name().equals(role)
            || video.getUploaderId().equals(currentUserId);
        if (!canRead) {
            throw new BusinessException("No permission to view this video");
        }
    }
}
