package com.taskflow.api.dashboard;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskflow.api.project.ProjectRepository;
import com.taskflow.api.task.TaskRepository;
import com.taskflow.api.task.TaskStatus;
import com.taskflow.api.user.UserRepository;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ProjectRepository projects;
    private final TaskRepository tasks;
    private final UserRepository users;

    public DashboardController(
            ProjectRepository projects,
            TaskRepository tasks,
            UserRepository users
    ) {
        this.projects = projects;
        this.tasks = tasks;
        this.users = users;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary(Authentication authentication) {

        Long userId = users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow()
                .getId();

        return Map.of(
                "projects", projects.countByOwnerId(userId),
                "tasks", tasks.countByOwnerId(userId),
                "todo", tasks.countByOwnerIdAndStatus(userId, TaskStatus.TODO),
                "inProgress", tasks.countByOwnerIdAndStatus(userId, TaskStatus.IN_PROGRESS),
                "done", tasks.countByOwnerIdAndStatus(userId, TaskStatus.DONE),
                "review", tasks.countByOwnerIdAndStatus(userId, TaskStatus.REVIEW)
        );
    }
}