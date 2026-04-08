package com.nobodies.platform.task.service;

import com.nobodies.platform.task.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskScheduler {

    private final TaskService taskService;
    private final ThumbnailGenerationService thumbnailGenerationService;
    private final VideoTranscodingService videoTranscodingService;

    @Scheduled(fixedRate = 5000)
    public void processPendingTasks() {
        List<Task> pendingTasks = taskService.getPendingTasks();
        
        for (Task task : pendingTasks) {
            try {
                taskService.updateTaskStatus(task.getId(), Task.TaskStatus.PROCESSING, 0, null);
                
                switch (task.getTaskType()) {
                    case THUMBNAIL_GENERATION:
                        thumbnailGenerationService.processTask(task);
                        break;
                    case VIDEO_TRANSCODING:
                        videoTranscodingService.processTask(task);
                        break;
                    case METADATA_EXTRACTION:
                        processMetadataExtraction(task);
                        break;
                    default:
                        log.warn("Unknown task type: {}", task.getTaskType());
                        taskService.updateTaskStatus(task.getId(), Task.TaskStatus.FAILED, 0, "Unknown task type");
                }
            } catch (Exception e) {
                log.error("Failed to process task {}: {}", task.getId(), e.getMessage(), e);
                taskService.incrementRetryCount(task.getId());
                
                if (task.getRetryCount() >= 3) {
                    taskService.updateTaskStatus(task.getId(), Task.TaskStatus.FAILED, 0, e.getMessage());
                } else {
                    taskService.updateTaskStatus(task.getId(), Task.TaskStatus.PENDING, 0, e.getMessage());
                }
            }
        }
    }

    private void processMetadataExtraction(Task task) {
        log.info("Processing metadata extraction task: {}", task.getId());
        
        try {
            taskService.updateTaskStatus(task.getId(), Task.TaskStatus.PROCESSING, 50, null);
            
            Thread.sleep(2000);
            
            taskService.updateTaskStatus(task.getId(), Task.TaskStatus.COMPLETED, 100, null);
            log.info("Metadata extraction completed for task: {}", task.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract metadata", e);
        }
    }
}