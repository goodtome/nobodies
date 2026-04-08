package com.nobodies.platform.upload.service;

import com.nobodies.platform.common.enums.UploadStatus;
import com.nobodies.platform.common.exception.BusinessException;
import com.nobodies.platform.security.LoginUser;
import com.nobodies.platform.storage.OssMultipartService;
import com.nobodies.platform.upload.dto.InitUploadRequest;
import com.nobodies.platform.upload.dto.InitUploadResponse;
import com.nobodies.platform.upload.dto.UploadConfigResponse;
import com.nobodies.platform.upload.dto.UploadProgressResponse;
import com.nobodies.platform.upload.entity.UploadChunk;
import com.nobodies.platform.upload.entity.UploadSession;
import com.nobodies.platform.upload.repository.UploadChunkRepository;
import com.nobodies.platform.upload.repository.UploadSessionRepository;
import com.nobodies.platform.video.repository.VideoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

    @Mock
    private UploadSessionRepository uploadSessionRepository;

    @Mock
    private UploadChunkRepository uploadChunkRepository;

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private OssMultipartService ossMultipartService;

    @InjectMocks
    private UploadService uploadService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(uploadService, "allowedTypesConfig", "mp4,mov");
        ReflectionTestUtils.setField(uploadService, "maxFileSize", Long.MAX_VALUE);
        ReflectionTestUtils.setField(uploadService, "chunkRetryTimes", 3);
        LoginUser loginUser = new LoginUser(1L, "tester@example.com", "pwd", "UPLOADER");
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void uploadChunkShouldRejectLateChunkForAbortedSession() {
        UploadSession session = buildSession(99L, UploadStatus.ABORTED.name(), 2);
        MockMultipartFile file = new MockMultipartFile("file", "chunk-1.part", "application/octet-stream", "late".getBytes());
        when(uploadSessionRepository.findById(99L)).thenReturn(Optional.of(session));

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> uploadService.uploadChunk(99L, 1, file)
        );

        assertEquals("Upload session is no longer accepting chunks", exception.getMessage());
        verify(ossMultipartService, never()).uploadPart(any(), any(), anyInt(), any(), anyLong());
        verify(uploadChunkRepository, never()).save(any());
        verify(uploadSessionRepository, never()).save(any());
    }

    @Test
    void getProgressShouldClearResidualChunksForAbortedSession() {
        UploadSession session = buildSession(100L, UploadStatus.ABORTED.name(), 3);
        UploadChunk residualChunk = new UploadChunk();
        residualChunk.setSessionId(100L);
        residualChunk.setChunkNumber(1);
        residualChunk.setChunkSize(1024L);
        residualChunk.setStatus(UploadStatus.UPLOADED.name());
        residualChunk.setEtag("etag-1");

        when(uploadSessionRepository.findById(100L)).thenReturn(Optional.of(session));
        when(uploadChunkRepository.findBySessionIdOrderByChunkNumberAsc(100L)).thenReturn(List.of(residualChunk));

        UploadProgressResponse response = uploadService.getProgress(100L);

        verify(uploadChunkRepository).deleteBySessionId(100L);
        assertEquals(UploadStatus.ABORTED.name(), response.getStatus());
        assertEquals(0, response.getUploadedChunksCount());
        assertEquals(0, response.getUploadedChunks().size());
        assertEquals(3, response.getAbortedChunksCount());
        assertEquals(0, response.getPendingChunksCount());
        assertEquals(3, response.getChunkStatuses().size());
        assertTrue(response.getChunkStatuses().stream().allMatch(chunk -> UploadStatus.ABORTED.name().equals(chunk.getStatus())));
        assertTrue(response.getChunkStatuses().stream().noneMatch(chunk -> Boolean.TRUE.equals(chunk.getUploaded())));
    }

    @Test
    void initUploadShouldReturnConfiguredChunkRetryTimes() {
        ReflectionTestUtils.setField(uploadService, "chunkRetryTimes", 5);
        UploadSession session = buildSession(101L, UploadStatus.INITIATED.name(), 2);
        InitUploadRequest request = buildInitUploadRequest();

        when(uploadSessionRepository.findFirstByUserIdAndFileNameAndFileSizeAndStatusInOrderByUpdatedAtDesc(
            1L,
            "demo.mp4",
            10_000L,
            List.of(UploadStatus.INITIATED.name(), UploadStatus.IN_PROGRESS.name())
        )).thenReturn(Optional.of(session));
        when(uploadChunkRepository.findBySessionIdOrderByChunkNumberAsc(101L)).thenReturn(List.of());

        InitUploadResponse response = uploadService.initUpload(request);

        assertEquals(5, response.getChunkRetryTimes());
    }

    @Test
    void initUploadShouldAllowZeroChunkRetryTimes() {
        ReflectionTestUtils.setField(uploadService, "chunkRetryTimes", 0);
        UploadSession session = buildSession(102L, UploadStatus.INITIATED.name(), 2);
        InitUploadRequest request = buildInitUploadRequest();

        when(uploadSessionRepository.findFirstByUserIdAndFileNameAndFileSizeAndStatusInOrderByUpdatedAtDesc(
            1L,
            "demo.mp4",
            10_000L,
            List.of(UploadStatus.INITIATED.name(), UploadStatus.IN_PROGRESS.name())
        )).thenReturn(Optional.of(session));
        when(uploadChunkRepository.findBySessionIdOrderByChunkNumberAsc(102L)).thenReturn(List.of());

        InitUploadResponse response = uploadService.initUpload(request);

        assertEquals(0, response.getChunkRetryTimes());
    }

    @Test
    void initUploadShouldRejectNegativeChunkRetryTimes() {
        ReflectionTestUtils.setField(uploadService, "chunkRetryTimes", -1);
        InitUploadRequest request = buildInitUploadRequest();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> uploadService.initUpload(request));

        assertEquals("upload.chunk-retry-times must be greater than or equal to 0", exception.getMessage());
    }

    @Test
    void abortUploadShouldAbortMultipartAndClearChunks() {
        UploadSession session = buildSession(103L, UploadStatus.IN_PROGRESS.name(), 3);
        when(uploadSessionRepository.findById(103L)).thenReturn(Optional.of(session));

        uploadService.abortUpload(103L);

        verify(ossMultipartService).abortMultipartUpload("videos/1/demo.mp4", "oss-upload-id");
        verify(uploadChunkRepository).deleteBySessionId(103L);
        verify(uploadSessionRepository).save(argThat(saved ->
            UploadStatus.ABORTED.name().equals(saved.getStatus()) && saved.getId().equals(103L)
        ));
    }

    @Test
    void getUploadConfigShouldReturnConfiguredLimits() {
        ReflectionTestUtils.setField(uploadService, "allowedTypesConfig", "mp4,mov,avi");
        ReflectionTestUtils.setField(uploadService, "maxFileSize", 12345L);

        UploadConfigResponse response = uploadService.getUploadConfig();

        assertEquals(12345L, response.getMaxFileSize());
        assertEquals(List.of("avi", "mov", "mp4"), response.getAllowedTypes());
    }

    private InitUploadRequest buildInitUploadRequest() {
        InitUploadRequest request = new InitUploadRequest();
        request.setFileName("demo.mp4");
        request.setFileSize(10_000L);
        request.setFileType("mp4");
        request.setChunkSize(5_000L);
        return request;
    }

    private UploadSession buildSession(Long sessionId, String status, int totalChunks) {
        UploadSession session = new UploadSession();
        session.setId(sessionId);
        session.setUserId(1L);
        session.setFileName("demo.mp4");
        session.setFileSize(10_000L);
        session.setFileType("mp4");
        session.setChunkSize(5_000L);
        session.setTotalChunks(totalChunks);
        session.setObjectKey("videos/1/demo.mp4");
        session.setOssUploadId("oss-upload-id");
        session.setStatus(status);
        return session;
    }
}