package com.nobodies.platform.upload.repository;

import com.nobodies.platform.upload.entity.UploadChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UploadChunkRepository extends JpaRepository<UploadChunk, Long> {
    Optional<UploadChunk> findBySessionIdAndChunkNumber(Long sessionId, Integer chunkNumber);

    List<UploadChunk> findBySessionIdOrderByChunkNumberAsc(Long sessionId);

    List<UploadChunk> findBySessionId(Long sessionId);

    void deleteBySessionId(Long sessionId);
}
