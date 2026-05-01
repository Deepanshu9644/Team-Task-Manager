package com.taskmanager.dto.request;

import com.taskmanager.enums.TaskPriority;
import com.taskmanager.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

public class TaskRequest {

    @Data
    public static class Create {
        @NotBlank(message = "Task title is required")
        @Size(min = 2, max = 200)
        private String title;

        @Size(max = 2000)
        private String description;

        private TaskPriority priority;

        private LocalDate dueDate;

        private Long assigneeId;
    }

    @Data
    public static class Update {
        @Size(min = 2, max = 200)
        private String title;

        @Size(max = 2000)
        private String description;

        private TaskStatus status;

        private TaskPriority priority;

        private LocalDate dueDate;

        private Long assigneeId;
    }
}
