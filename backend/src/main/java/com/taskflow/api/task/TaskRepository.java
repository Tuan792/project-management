package com.taskflow.api.task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TaskRepository extends JpaRepository<TaskItem,Long> {
 List<TaskItem> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);
 Optional<TaskItem> findByIdAndOwnerId(Long id,Long ownerId);
 long countByOwnerId(Long ownerId);
 long countByOwnerIdAndStatus(Long ownerId,TaskStatus status);
}
