
package com.taskflow.api.task;

import java.time.Instant;
import java.time.LocalDate;

import com.taskflow.api.project.Project;
import com.taskflow.api.user.AppUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class TaskItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;

    private LocalDate dueDate;

    @Column(nullable = false, updatable = false)
    private final Instant createdAt = Instant.now();

    protected TaskItem() {
        // Constructor mặc định dành cho JPA.
    }

    public TaskItem(
            AppUser owner,
            Project project,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate
    ) {
        this.owner = owner;
        this.project = project;
        this.title = title;
        this.description = description;
        this.status = status != null
                ? status
                : TaskStatus.TODO;
        this.priority = priority != null
                ? priority
                : TaskPriority.MEDIUM;
        this.dueDate = dueDate;
    }

    public void update(
            Project project,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate
    ) {
        this.project = project;
        this.title = title;
        this.description = description;

        if (status != null) {
            this.status = status;
        }

        if (priority != null) {
            this.priority = priority;
        }

        this.dueDate = dueDate;
    }

    public Long getId() {
        return id;
    }

    public AppUser getOwner() {
        return owner;
    }

    public Project getProject() {
        return project;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
