CREATE TABLE upload_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(32) NOT NULL,
    chunk_size BIGINT NOT NULL,
    total_chunks INT NOT NULL,
    object_key VARCHAR(512) NOT NULL,
    oss_upload_id VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_upload_sessions_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE upload_chunks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    chunk_number INT NOT NULL,
    etag VARCHAR(255),
    chunk_size BIGINT,
    status VARCHAR(32) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    uploaded_at DATETIME,
    CONSTRAINT fk_upload_chunks_session FOREIGN KEY (session_id) REFERENCES upload_sessions (id),
    CONSTRAINT uk_session_chunk UNIQUE (session_id, chunk_number)
);
