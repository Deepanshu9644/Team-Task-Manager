package com.taskmanager.dto.response;

import com.taskmanager.enums.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ApiResponse {

    // ─── Generic wrapper ────────────────────────────────────────────
    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class Success<T> {
        private boolean success = true;
        private String message;
        private T data;

        public static <T> Success<T> of(String message, T data) {
            return Success.<T>builder().success(true).message(message).data(data).build();
        }
        public static <T> Success<T> of(T data) {
            return Success.<T>builder().success(true).data(data).build();
        }
    }

    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class Error {
        private boolean success = false;
        private String message;
        private Object errors;
    }

    // ─── Auth ────────────────────────────────────────────────────────
    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class AuthToken {
        private String token;
        private String type = "Bearer";
        private UserInfo user;
    }

    // ─── User ────────────────────────────────────────────────────────
    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private Role role;
        private boolean active;
        private LocalDateTime createdAt;
    }

    // ─── Project ─────────────────────────────────────────────────────
    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class ProjectInfo {
        private Long id;
        private String name;
        private String description;
        private ProjectStatus status;
        private String color;
        private LocalDate dueDate;
        private UserInfo createdBy;
        private int memberCount;
        private long totalTasks;
        private long completedTasks;
        private int progressPercent;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class ProjectDetail {
        private Long id;
        private String name;
        private String description;
        private ProjectStatus status;
        private String color;
        private LocalDate dueDate;
        private UserInfo createdBy;
        private List<MemberInfo> members;
        private List<TaskInfo> tasks;
        private long totalTasks;
        private long completedTasks;
        private int progressPercent;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    // ─── Task ─────────────────────────────────────────────────────────
    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class TaskInfo {
        private Long id;
        private String title;
        private String description;
        private TaskStatus status;
        private TaskPriority priority;
        private LocalDate dueDate;
        private boolean overdue;
        private Long projectId;
        private String projectName;
        private UserInfo assignee;
        private UserInfo createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    // ─── Member ───────────────────────────────────────────────────────
    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class MemberInfo {
        private Long memberId;
        private UserInfo user;
        private Role role;
        private LocalDateTime joinedAt;
        private long tasksAssigned;
    }

    // ─── Dashboard ────────────────────────────────────────────────────
    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class DashboardStats {
        private long totalProjects;
        private long totalTasks;
        private long completedTasks;
        private long overdueTasks;
        private List<ProjectInfo> recentProjects;
        private List<TaskInfo> myTasks;
        private List<TaskInfo> overduedTasks;
    }
}
