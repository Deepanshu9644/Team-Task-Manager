package com.taskmanager.dto.request;

import com.taskmanager.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class MemberRequest {

    @Data
    public static class Invite {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotNull(message = "Role is required")
        private Role role;
    }

    @Data
    public static class UpdateRole {
        @NotNull(message = "Role is required")
        private Role role;
    }
}
