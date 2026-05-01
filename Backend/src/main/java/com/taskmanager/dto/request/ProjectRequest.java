package com.taskmanager.dto.request;

import com.taskmanager.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

public class ProjectRequest {

    @Data
    public static class Create {
        @NotBlank(message = "Project name is required")
        @Size(min = 2, max = 150)
        private String name;

        @Size(max = 500)
        private String description;

        private String color;

        private LocalDate dueDate;
    }

    @Data
    public static class Update {
        @Size(min = 2, max = 150)
        private String name;

        @Size(max = 500)
        private String description;

        private String color;

        private LocalDate dueDate;

        private ProjectStatus status;
    }
}
