package com.taskmanager.controller;

import com.taskmanager.dto.request.TaskRequest;
import com.taskmanager.dto.response.ApiResponse;
import com.taskmanager.entity.User;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final CurrentUserResolver userResolver;

    @PostMapping
    public ResponseEntity<ApiResponse.Success<ApiResponse.TaskInfo>> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskRequest.Create request) {
        User currentUser = userResolver.resolve();
        ApiResponse.TaskInfo task = taskService.createTask(projectId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.Success.of("Task created successfully", task));
    }

    @GetMapping
    public ResponseEntity<ApiResponse.Success<List<ApiResponse.TaskInfo>>> getProjectTasks(
            @PathVariable Long projectId) {
        User currentUser = userResolver.resolve();
        List<ApiResponse.TaskInfo> tasks = taskService.getProjectTasks(projectId, currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of(tasks));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse.Success<ApiResponse.TaskInfo>> getTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        User currentUser = userResolver.resolve();
        ApiResponse.TaskInfo task = taskService.getTask(projectId, taskId, currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of(task));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse.Success<ApiResponse.TaskInfo>> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequest.Update request) {
        User currentUser = userResolver.resolve();
        ApiResponse.TaskInfo task = taskService.updateTask(projectId, taskId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of("Task updated successfully", task));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<ApiResponse.Success<ApiResponse.TaskInfo>> updateStatus(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody Map<String, String> body) {
        User currentUser = userResolver.resolve();
        TaskStatus status = TaskStatus.valueOf(body.get("status").toUpperCase());
        ApiResponse.TaskInfo task = taskService.updateTaskStatus(projectId, taskId, status, currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of("Task status updated", task));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse.Success<Void>> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        User currentUser = userResolver.resolve();
        taskService.deleteTask(projectId, taskId, currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of("Task deleted successfully", null));
    }
}
