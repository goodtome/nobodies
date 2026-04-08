package com.nobodies.platform.upload.service;

import com.nobodies.platform.common.enums.UploadStatus;
import com.nobodies.platform.common.enums.VideoStatus;
import com.nobodies.platform.common.exception.BusinessException;
import com.nobodies.platform.common.util.FileTypeUtil;
import com.nobodies.platform.common.util.SecurityUtil;
import com.nobodies.platform.storage.OssMultipartService;
import com.nobodies.platform.upload.dto.CompleteUploadRequest;
import com.nobodies.platform.upload.dto.InitUploadRequest;
import com.nobodies.platform.upload.dto.InitUploadResponse;
import com.nobodies.platform.upload.dto.UploadConfigResponse;
import com.nobodies.platform.upload.dto.UploadChunkResponse;
import com.nobodies.platform.upload.dto.UploadChunkStatusResponse;
import com.nobodies.platform.upload.dto.UploadProgressResponse;
import com.nobodies.platform.upload.entity.UploadChunk;
import com.nobodies.platform.upload.entity.UploadSession;
import com.nobodies.platform.upload.repository.UploadChunkRepository;
import com.nobodies.platform.upload.repository.UploadSessionRepository;
import com.nobodies.platform.video.entity.Video;
import com.nobodies.platform.task.entity.Task;
import com.nobodies.platform.task.service.TaskService;
import com.nobodies.platform.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class UploadService {

    private static final String CHUNK_STATUS_PENDING = "PENDING";

    private final UploadSessionRepository uploadSessionRepository;
    private final UploadChunkRepository uploadChunkRepository;
    private final VideoRepository videoRepository;
    private final OssMultipartService ossMultipartService;
    private final TaskService taskService;

    @Value("${upload.allowed-types}")
    private String allowedTypesConfig;

    @Value("${upload.max-file-size}")
    private long maxFileSize;

    @Value("${upload.chunk-retry-times}")
    private int chunkRetryTimes;

    public UploadConfigResponse getUploadConfig() {
        validateChunkRetryTimes();
        return UploadConfigResponse.builder()
            .maxFileSize(maxFileSize)
            .allowedTypes(FileTypeUtil.parseAllowedTypes(allowedTypesConfig).stream().sorted().toList())
            .build();
    }
    public InitUploadResponse initUpload(InitUploadRequest request) {
        validateFile(request);
        validateChunkRetryTimes();
        Long userId = requireCurrentUserId();
        UploadSession session = uploadSessionRepository
            .findFirstByUserIdAndFileNameAndFileSizeAndStatusInOrderByUpdatedAtDesc(
                userId,
                request.getFileName(),
                request.getFileSize(),
                List.of(UploadStatus.INITIATED.name(), UploadStatus.IN_PROGRESS.name())
            )
            .orElseGet(() -> createSession(userId, request));

        List<UploadChunkStatusResponse> chunkStatuses = buildChunkStatuses(session, loadChunkMap(session.getId()));
        List<Integer> uploadedChunks = extractUploadedChunks(chunkStatuses);

        return InitUploadResponse.builder()
            .uploadSessionId(session.getId())
            .objectKey(session.getObjectKey())
            .ossUploadId(session.getOssUploadId())
            .totalChunks(session.getTotalChunks())
            .chunkRetryTimes(chunkRetryTimes)
            .uploadedChunksCount(uploadedChunks.size())
            .pendingChunksCount(countByStatus(chunkStatuses, CHUNK_STATUS_PENDING))
            .abortedChunksCount(countByStatus(chunkStatuses, UploadStatus.ABORTED.name()))
            .uploadedChunks(uploadedChunks)
            .chunkStatuses(chunkStatuses)
            .build();
    }

    @Transactional
    public UploadChunkResponse uploadChunk(Long sessionId, Integer chunkNumber, MultipartFile chunkFile) {
        UploadSession session = getOwnedSession(sessionId);
        validateChunkRequest(session, chunkNumber, chunkFile);
        ensureSessionAcceptsChunkUpload(session);
        session.setStatus(UploadStatus.IN_PROGRESS.name());
        uploadSessionRepository.save(session);

        try {
            UploadChunk existingChunk = uploadChunkRepository.findBySessionIdAndChunkNumber(sessionId, chunkNumber).orElse(null);
            if (existingChunk != null) {
                return new UploadChunkResponse(sessionId, chunkNumber, existingChunk.getStatus(), existingChunk.getEtag());
            }

            String etag = ossMultipartService.uploadPart(
                session.getObjectKey(),
                session.getOssUploadId(),
                chunkNumber,
                chunkFile.getInputStream(),
                chunkFile.getSize()
            );

            UploadChunk chunk = new UploadChunk();
            chunk.setSessionId(sessionId);
            chunk.setChunkNumber(chunkNumber);
            chunk.setEtag(etag);
            chunk.setChunkSize(chunkFile.getSize());
            chunk.setStatus(UploadStatus.UPLOADED.name());
            chunk.setUploadedAt(LocalDateTime.now());
            uploadChunkRepository.save(chunk);

            return new UploadChunkResponse(sessionId, chunkNumber, UploadStatus.UPLOADED.name(), etag);
        } catch (IOException ex) {
            throw new BusinessException("Failed to read chunk content");
        }
    }

    @Transactional
    public void completeUpload(Long sessionId, CompleteUploadRequest request) {
        UploadSession session = getOwnedSession(sessionId);
        if (UploadStatus.ABORTED.name().equals(session.getStatus())) {
            throw new BusinessException("Upload session has been aborted");
        }
        List<UploadChunk> chunks = uploadChunkRepository.findBySessionIdOrderByChunkNumberAsc(sessionId);
        if (chunks.size() != session.getTotalChunks()) {
            throw new BusinessException("Not all chunks have been uploaded");
        }

        ossMultipartService.completeMultipartUpload(session.getObjectKey(), session.getOssUploadId(), chunks);
        session.setStatus(UploadStatus.COMPLETED.name());
        uploadSessionRepository.save(session);

        Video video = new Video();
        video.setTitle(request.getTitle());
        video.setUploaderId(session.getUserId());
        video.setFileName(session.getFileName());
        video.setFileSize(session.getFileSize());
        video.setFileType(session.getFileType());
        video.setOssKey(session.getObjectKey());
        video.setStatus(VideoStatus.AVAILABLE.name());
        video.setDeleted(false);
        Video savedVideo = videoRepository.save(video);

        createPostUploadTasks(savedVideo.getId(), session.getObjectKey());
    }

    @Transactional
    public void abortUpload(Long sessionId) {
        UploadSession session = getOwnedSession(sessionId);
        if (!UploadStatus.ABORTED.name().equals(session.getStatus())) {
            ossMultipartService.abortMultipartUpload(session.getObjectKey(), session.getOssUploadId());
        }
        clearChunkRecords(sessionId);
        session.setStatus(UploadStatus.ABORTED.name());
        uploadSessionRepository.save(session);
    }

    @Transactional
    public UploadProgressResponse getProgress(Long sessionId) {
        UploadSession session = getOwnedSession(sessionId);
        Map<Integer, UploadChunk> chunkMap = loadChunkMap(sessionId);
        if (UploadStatus.ABORTED.name().equals(session.getStatus()) && !chunkMap.isEmpty()) {
            clearChunkRecords(sessionId);
            chunkMap = Map.of();
        }

        List<UploadChunkStatusResponse> chunkStatuses = buildChunkStatuses(session, chunkMap);
        List<Integer> uploadedChunks = extractUploadedChunks(chunkStatuses);

        return UploadProgressResponse.builder()
            .sessionId(session.getId())
            .status(session.getStatus())
            .totalChunks(session.getTotalChunks())
            .uploadedChunksCount(uploadedChunks.size())
            .pendingChunksCount(countByStatus(chunkStatuses, CHUNK_STATUS_PENDING))
            .abortedChunksCount(countByStatus(chunkStatuses, UploadStatus.ABORTED.name()))
            .uploadedChunks(uploadedChunks)
            .chunkStatuses(chunkStatuses)
            .build();
    }

    private UploadSession getOwnedSession(Long sessionId) {
        Long currentUserId = requireCurrentUserId();
        UploadSession session = uploadSessionRepository.findById(sessionId)
            .orElseThrow(() -> new BusinessException("Upload session not found"));
        if (!session.getUserId().equals(currentUserId)) {
            throw new BusinessException("No permission to access this upload session");
        }
        return session;
    }

    private Long requireCurrentUserId() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("Current user not found");
        }
        return currentUserId;
    }

    private void validateChunkRequest(UploadSession session, Integer chunkNumber, MultipartFile chunkFile) {
        if (chunkNumber == null || chunkNumber < 1 || chunkNumber > session.getTotalChunks()) {
            throw new BusinessException("Chunk number is out of range");
        }
        if (chunkFile == null || chunkFile.isEmpty()) {
            throw new BusinessException("Chunk file is required");
        }
    }

    private void ensureSessionAcceptsChunkUpload(UploadSession session) {
        if (UploadStatus.ABORTED.name().equals(session.getStatus()) || UploadStatus.COMPLETED.name().equals(session.getStatus())) {
            throw new BusinessException("Upload session is no longer accepting chunks");
        }
    }

    private void validateFile(InitUploadRequest request) {
        if (request.getFileSize() == null || request.getFileSize() <= 0) {
            throw new BusinessException("File size must be greater than 0");
        }
        if (request.getChunkSize() == null || request.getChunkSize() <= 0) {
            throw new BusinessException("Chunk size must be greater than 0");
        }
        if (request.getFileSize() > maxFileSize) {
            throw new BusinessException("File size exceeds configured limit");
        }
        Set<String> allowedTypes = FileTypeUtil.parseAllowedTypes(allowedTypesConfig);
        String normalizedType = request.getFileType().toLowerCase();
        if (!allowedTypes.contains(normalizedType)) {
            throw new BusinessException("Unsupported file type");
        }
    }

    private void validateChunkRetryTimes() {
        if (chunkRetryTimes < 0) {
            throw new IllegalStateException("upload.chunk-retry-times must be greater than or equal to 0");
        }
    }

    private UploadSession createSession(Long userId, InitUploadRequest request) {
        String objectKey = buildObjectKey(userId, request.getFileName());
        String ossUploadId = ossMultipartService.initMultipartUpload(objectKey, request.getFileType());
        int totalChunks = (int) Math.ceil((double) request.getFileSize() / request.getChunkSize());

        UploadSession session = new UploadSession();
        session.setUserId(userId);
        session.setFileName(request.getFileName());
        session.setFileSize(request.getFileSize());
        session.setFileType(request.getFileType().toLowerCase());
        session.setChunkSize(request.getChunkSize());
        session.setTotalChunks(totalChunks);
        session.setObjectKey(objectKey);
        session.setOssUploadId(ossUploadId);
        session.setStatus(UploadStatus.INITIATED.name());
        return uploadSessionRepository.save(session);
    }

    private String buildObjectKey(Long userId, String fileName) {
        return "videos/" + userId + "/" + System.currentTimeMillis() + "-" + fileName;
    }

    private Map<Integer, UploadChunk> loadChunkMap(Long sessionId) {
        return uploadChunkRepository.findBySessionIdOrderByChunkNumberAsc(sessionId).stream()
            .collect(Collectors.toMap(UploadChunk::getChunkNumber, Function.identity(), (left, right) -> right));
    }

    private List<UploadChunkStatusResponse> buildChunkStatuses(UploadSession session, Map<Integer, UploadChunk> chunkMap) {
        boolean abortedSession = UploadStatus.ABORTED.name().equals(session.getStatus());
        return IntStream.rangeClosed(1, session.getTotalChunks())
            .mapToObj(chunkNumber -> buildChunkStatus(chunkNumber, chunkMap.get(chunkNumber), abortedSession))
            .toList();
    }

    private UploadChunkStatusResponse buildChunkStatus(Integer chunkNumber, UploadChunk chunk, boolean abortedSession) {
        if (chunk != null) {
            return UploadChunkStatusResponse.builder()
                .chunkNumber(chunk.getChunkNumber())
                .status(chunk.getStatus())
                .chunkSize(chunk.getChunkSize())
                .retryCount(chunk.getRetryCount())
                .etag(chunk.getEtag())
                .uploadedAt(chunk.getUploadedAt() == null ? null : chunk.getUploadedAt().toString())
                .uploaded(Boolean.TRUE)
                .build();
        }

        return UploadChunkStatusResponse.builder()
            .chunkNumber(chunkNumber)
            .status(abortedSession ? UploadStatus.ABORTED.name() : CHUNK_STATUS_PENDING)
            .chunkSize(null)
            .retryCount(0)
            .etag(null)
            .uploadedAt(null)
            .uploaded(Boolean.FALSE)
            .build();
    }

    private List<Integer> extractUploadedChunks(List<UploadChunkStatusResponse> chunkStatuses) {
        return chunkStatuses.stream()
            .filter(chunk -> Boolean.TRUE.equals(chunk.getUploaded()))
            .map(UploadChunkStatusResponse::getChunkNumber)
            .toList();
    }

    private Integer countByStatus(List<UploadChunkStatusResponse> chunkStatuses, String status) {
        return Math.toIntExact(chunkStatuses.stream()
            .filter(chunk -> status.equals(chunk.getStatus()))
            .count());
    }

    private void clearChunkRecords(Long sessionId) {
        uploadChunkRepository.deleteBySessionId(sessionId);
    }

    private void createPostUploadTasks(Long videoId, String objectKey) {
        taskService.createTask(Task.TaskType.THUMBNAIL_GENERATION, videoId, Map.of("objectKey", objectKey), 2);
        taskService.createTask(Task.TaskType.VIDEO_TRANSCODING, videoId, Map.of("objectKey", objectKey), 1);
        taskService.createTask(Task.TaskType.METADATA_EXTRACTION, videoId, Map.of("objectKey", objectKey), 3);
    }
}

