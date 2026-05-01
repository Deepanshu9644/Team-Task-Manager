package com.taskmanager.controller;

import com.taskmanager.dto.response.ApiResponse;
import com.taskmanager.entity.User;
import com.taskmanager.service.DashboardService;
import com.taskmanager.service.TaskService;
import com.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final TaskService taskService;
    private final UserService userService;
    private final CurrentUserResolver userResolver;

    // ─── Dashboard ─────────────────────────────────────────────────────
    @GetMapping("/api/dashboard")
    public ResponseEntity<ApiResponse.Success<ApiResponse.DashboardStats>> getDashboard() {
        User currentUser = userResolver.resolve();
        ApiResponse.DashboardStats stats = dashboardService.getDashboard(currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of(stats));
    }

    // ─── My Tasks ──────────────────────────────────────────────────────
    @GetMapping("/api/tasks/my")
    public ResponseEntity<ApiResponse.Success<List<ApiResponse.TaskInfo>>> getMyTasks() {
        User currentUser = userResolver.resolve();
        List<ApiResponse.TaskInfo> tasks = taskService.getMyTasks(currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of(tasks));
    }

    @GetMapping("/api/tasks/overdue")
    public ResponseEntity<ApiResponse.Success<List<ApiResponse.TaskInfo>>> getOverdueTasks() {
        User currentUser = userResolver.resolve();
        List<ApiResponse.TaskInfo> tasks = taskService.getMyOverdueTasks(currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of(tasks));
    }

    // ─── User Profile ──────────────────────────────────────────────────
    @GetMapping("/api/users/me")
    public ResponseEntity<ApiResponse.Success<ApiResponse.UserInfo>> getProfile() {
        User currentUser = userResolver.resolve();
        ApiResponse.UserInfo profile = userService.getProfile(currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of(profile));
    }

    @GetMapping("/api/users")
    public ResponseEntity<ApiResponse.Success<List<ApiResponse.UserInfo>>> getAllUsers() {
        List<ApiResponse.UserInfo> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.Success.of(users));
    }

    @GetMapping("/api/users/{id}")
    public ResponseEntity<ApiResponse.Success<ApiResponse.UserInfo>> getUserById(@PathVariable Long id) {
        ApiResponse.UserInfo user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.Success.of(user));
    }

    // ─── Health ────────────────────────────────────────────────────────
    @GetMapping("/api/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Task Manager API is running ✅");
    }
}
