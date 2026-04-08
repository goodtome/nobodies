package com.nobodies.platform.task.service;

import com.nobodies.platform.task.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoTranscodingService {

    private final TaskService taskService;

    public void processTask(Task task) {
        log.info("Processing video transcoding task: {}", task.getId());
        
        try {
            taskService.updateTaskStatus(task.getId(), Task.TaskStatus.PROCESSING, 10, null);
            
            for (int i = 20; i <= 90; i += 10) {
                Thread.sleep(1500);
                taskService.updateTaskStatus(task.getId(), Task.TaskStatus.PROCESSING, i, null);
            }
            
            Thread.sleep(2000);
            
            taskService.updateTaskStatus(task.getId(), Task.TaskStatus.COMPLETED, 100, null);
            log.info("Video transcoding completed for task: {}", task.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to transcode video", e);
        }
    }
}