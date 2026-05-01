package com.taskmanager.controller;

import com.taskmanager.dto.request.ProjectRequest;
import com.taskmanager.dto.response.ApiResponse;
import com.taskmanager.entity.User;
import com.taskmanager.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final CurrentUserResolver userResolver;

    @PostMapping
    public ResponseEntity<ApiResponse.Success<ApiResponse.ProjectInfo>> createProject(
            @Valid @RequestBody ProjectRequest.Create request) {
        User currentUser = userResolver.resolve();
        ApiResponse.ProjectInfo project = projectService.createProject(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.Success.of("Project created successfully", project));
    }

    @GetMapping
    public ResponseEntity<ApiResponse.Success<List<ApiResponse.ProjectInfo>>> getAllProjects() {
        User currentUser = userResolver.resolve();
        List<ApiResponse.ProjectInfo> projects = projectService.getAllProjects(currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of(projects));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse.Success<ApiResponse.ProjectDetail>> getProject(
            @PathVariable Long projectId) {
        User currentUser = userResolver.resolve();
        ApiResponse.ProjectDetail project = projectService.getProjectDetail(projectId, currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of(project));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponse.Success<ApiResponse.ProjectInfo>> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequest.Update request) {
        User currentUser = userResolver.resolve();
        ApiResponse.ProjectInfo project = projectService.updateProject(projectId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of("Project updated successfully", project));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse.Success<Void>> deleteProject(@PathVariable Long projectId) {
        User currentUser = userResolver.resolve();
        projectService.deleteProject(projectId, currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of("Project deleted successfully", null));
    }
}
