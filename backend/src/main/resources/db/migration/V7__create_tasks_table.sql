CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_type VARCHAR(50) NOT NULL,
    video_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    priority INTEGER NOT NULL,
    payload JSON,
    progress INTEGER,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,
    
    INDEX idx_task_type (task_type),
    INDEX idx_video_id (video_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE
);