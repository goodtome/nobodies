package com.nobodies.platform.upload.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "upload_chunks")
public class UploadChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "chunk_number", nullable = false)
    private Integer chunkNumber;

    private String etag;

    @Column(name = "chunk_size")
    private Long chunkSize;

    @Column(nullable = false)
    private String status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
}
