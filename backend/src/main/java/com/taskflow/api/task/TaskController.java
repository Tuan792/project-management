
package com.taskflow.api.task;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.taskflow.api.project.Project;
import com.taskflow.api.project.ProjectRepository;
import com.taskflow.api.user.AppUser;
import com.taskflow.api.user.UserRepository;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository tasks;
    private final ProjectRepository projects;
    private final UserRepository users;

    public TaskController(
            TaskRepository tasks,
            ProjectRepository projects,
            UserRepository users
    ) {
        this.tasks = tasks;
        this.projects = projects;
        this.users = users;
    }

    public record TaskRequest(
            Long projectId,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate
    ) {
    }

    private Long uid(Authentication authentication) {
        return users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow()
                .getId();
    }

    private Project project(Long projectId, Long userId) {
        return projects.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy dự án"
                ));
    }

    private TaskItem owned(Long taskId, Long userId) {
        return tasks.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy công việc"
                ));
    }

    private Map<String, Object> dto(TaskItem task) {
        return Map.of(
                "id", task.getId(),
                "projectId", task.getProject().getId(),
                "projectName", task.getProject().getName(),
                "title", task.getTitle(),
                "description", task.getDescription() == null
                        ? ""
                        : task.getDescription(),
                "status", task.getStatus().name(),
                "priority", task.getPriority().name(),
                "dueDate", task.getDueDate() == null
                        ? ""
                        : task.getDueDate().toString()
        );
    }

    @GetMapping
    public List<Map<String, Object>> all(Authentication authentication) {
        return tasks
                .findAllByOwnerIdOrderByCreatedAtDesc(uid(authentication))
                .stream()
                .map(this::dto)
                .toList();
    }

    @PostMapping
    public Map<String, Object> create(
            @RequestBody TaskRequest request,
            Authentication authentication
    ) {
        Long userId = uid(authentication);

        if (request.title() == null || request.title().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên công việc là bắt buộc"
            );
        }

        AppUser user = users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow();

        Project project = project(request.projectId(), userId);

        TaskItem task = new TaskItem(
                user,
                project,
                request.title().trim(),
                request.description(),
                request.status(),
                request.priority(),
                request.dueDate()
        );

        return dto(tasks.save(task));
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(
            @PathVariable Long id,
            @RequestBody TaskRequest request,
            Authentication authentication
    ) {
        Long userId = uid(authentication);
        TaskItem task = owned(id, userId);

        if (request.title() == null || request.title().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên công việc là bắt buộc"
            );
        }

        Project project = project(request.projectId(), userId);

        task.update(
                project,
                request.title().trim(),
                request.description(),
                request.status(),
                request.priority(),
                request.dueDate()
        );

        return dto(tasks.save(task));
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        tasks.delete(owned(id, uid(authentication)));
    }
}
