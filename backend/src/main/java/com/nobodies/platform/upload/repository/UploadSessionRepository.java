package com.nobodies.platform.upload.repository;

import com.nobodies.platform.upload.entity.UploadSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface UploadSessionRepository extends JpaRepository<UploadSession, Long> {
    Optional<UploadSession> findFirstByUserIdAndFileNameAndFileSizeAndStatusInOrderByUpdatedAtDesc(
        Long userId,
        String fileName,
        Long fileSize,
        Collection<String> statuses
    );
}
