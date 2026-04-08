package com.nobodies.platform.task.repository;

import com.nobodies.platform.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatusOrderByPriorityDescCreatedAtAsc(Task.TaskStatus status);

    List<Task> findByVideoIdAndTaskType(Long videoId, Task.TaskType taskType);

    @Query("SELECT t FROM Task t WHERE t.status = :status AND t.createdAt < :cutoffTime ORDER BY t.priority DESC, t.createdAt ASC")
    List<Task> findPendingTasksOlderThan(@Param("status") Task.TaskStatus status, @Param("cutoffTime") LocalDateTime cutoffTime);

    long countByStatus(Task.TaskStatus status);

    List<Task> findByStatusAndRetryCountLessThanOrderByPriorityDescCreatedAtAsc(Task.TaskStatus status, Integer maxRetryCount);
}