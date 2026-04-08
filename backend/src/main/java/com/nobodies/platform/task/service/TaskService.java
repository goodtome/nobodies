package com.nobodies.platform.task.service;

import com.nobodies.platform.task.entity.Task;
import com.nobodies.platform.task.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Task createTask(Task.TaskType taskType, Long videoId, Map<String, Object> payload, Integer priority) {
        Task task = Task.builder()
            .taskType(taskType)
            .videoId(videoId)
            .status(Task.TaskStatus.PENDING)
            .priority(priority != null ? priority : 1)
            .payload(convertPayloadToString(payload))
            .progress(0)
            .retryCount(0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        return taskRepository.save(task);
    }

    @Transactional
    public void updateTaskStatus(Long taskId, Task.TaskStatus status, Integer progress, String errorMessage) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        
        task.setStatus(status);
        if (progress != null) {
            task.setProgress(progress);
        }
        if (errorMessage != null) {
            task.setErrorMessage(errorMessage);
        }
        if (status == Task.TaskStatus.COMPLETED || status == Task.TaskStatus.FAILED) {
            task.setProcessedAt(LocalDateTime.now());
        }
        task.setUpdatedAt(LocalDateTime.now());
        
        taskRepository.save(task);
    }

    @Transactional
    public void incrementRetryCount(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        
        task.setRetryCount(task.getRetryCount() + 1);
        task.setUpdatedAt(LocalDateTime.now());
        
        taskRepository.save(task);
    }

    public List<Task> getPendingTasks() {
        return taskRepository.findByStatusOrderByPriorityDescCreatedAtAsc(Task.TaskStatus.PENDING);
    }

    public List<Task> getTasksByVideoIdAndType(Long videoId, Task.TaskType taskType) {
        return taskRepository.findByVideoIdAndTaskType(videoId, taskType);
    }

    public long countTasksByStatus(Task.TaskStatus status) {
        return taskRepository.countByStatus(status);
    }

    public List<Task> getRetryableTasks(Integer maxRetryCount) {
        return taskRepository.findByStatusAndRetryCountLessThanOrderByPriorityDescCreatedAtAsc(
            Task.TaskStatus.FAILED, maxRetryCount);
    }

    private String convertPayloadToString(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert payload to JSON", e);
        }
    }
}