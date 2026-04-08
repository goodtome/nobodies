package com.nobodies.platform.task.service;

import com.nobodies.platform.task.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThumbnailGenerationService {

    private final TaskService taskService;

    public void processTask(Task task) {
        log.info("Processing thumbnail generation task: {}", task.getId());
        
        try {
            taskService.updateTaskStatus(task.getId(), Task.TaskStatus.PROCESSING, 25, null);
            
            Thread.sleep(3000);
            
            taskService.updateTaskStatus(task.getId(), Task.TaskStatus.PROCESSING, 75, null);
            
            Thread.sleep(2000);
            
            taskService.updateTaskStatus(task.getId(), Task.TaskStatus.COMPLETED, 100, null);
            log.info("Thumbnail generation completed for task: {}", task.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate thumbnail", e);
        }
    }
}