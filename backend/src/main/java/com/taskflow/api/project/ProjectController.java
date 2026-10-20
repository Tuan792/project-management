
package com.taskflow.api.project;

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

import com.taskflow.api.user.AppUser;
import com.taskflow.api.user.UserRepository;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projects;
    private final UserRepository users;

    public ProjectController(
            ProjectRepository projects,
            UserRepository users
    ) {
        this.projects = projects;
        this.users = users;
    }

    public record ProjectRequest(
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            ProjectStatus status
    ) {
    }

    private Long userId(Authentication authentication) {
        return users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow()
                .getId();
    }

    private Project owned(Long id, Long userId) {
        return projects.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy dự án"
                ));
    }

    private Map<String, Object> dto(Project project) {
        return Map.of(
                "id", project.getId(),
                "name", project.getName(),
                "description", project.getDescription() == null
                        ? ""
                        : project.getDescription(),
                "startDate", project.getStartDate() == null
                        ? ""
                        : project.getStartDate().toString(),
                "endDate", project.getEndDate() == null
                        ? ""
                        : project.getEndDate().toString(),
                "status", project.getStatus().name()
        );
    }

    @GetMapping
    public List<Map<String, Object>> all(Authentication authentication) {
        Long currentUserId = userId(authentication);

        return projects
                .findAllByOwnerIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(this::dto)
                .toList();
    }

    @PostMapping
    public Map<String, Object> create(
            @RequestBody ProjectRequest request,
            Authentication authentication
    ) {
        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên dự án là bắt buộc"
            );
        }

        AppUser user = users
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow();

        Project project = new Project(
                user,
                user, // Người tạo đồng thời là người quản lý
                request.name().trim(),
                request.description(),
                request.startDate(),
                request.endDate(),
                request.status()
        );

        return dto(projects.save(project));
    }

    @GetMapping("/{id}")
    public Map<String, Object> one(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return dto(owned(id, userId(authentication)));
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(
            @PathVariable Long id,
            @RequestBody ProjectRequest request,
            Authentication authentication
    ) {
        Project project = owned(id, userId(authentication));

        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên dự án là bắt buộc"
            );
        }

        project.update(
                request.name().trim(),
                request.description(),
                request.startDate(),
                request.endDate(),
                request.status()
        );

        return dto(projects.save(project));
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Project project = owned(id, userId(authentication));
        projects.delete(project);
    }
}
