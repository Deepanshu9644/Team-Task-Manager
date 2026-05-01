package com.taskmanager.service;

import com.taskmanager.dto.response.ApiResponse;
import com.taskmanager.entity.User;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final TaskService taskService;

    @Transactional(readOnly = true)
    public ApiResponse.DashboardStats getDashboard(User currentUser) {

        // Project stats
        List<ApiResponse.ProjectInfo> allProjects = projectRepository
                .findAllByUserMembership(currentUser)
                .stream()
                .map(projectService::toProjectInfo)
                .collect(Collectors.toList());

        // Task stats
        long totalTasks = taskRepository.countByAssignee(currentUser);
        long completedTasks = taskRepository.countCompletedByAssignee(currentUser);
        long overdueTasks = taskRepository.countOverdueByAssignee(currentUser, LocalDate.now());

        // My tasks
        List<ApiResponse.TaskInfo> myTasks = taskRepository.findByAssignee(currentUser)
                .stream()
                .map(taskService::toTaskInfo)
                .collect(Collectors.toList());

        // Overdue tasks
        List<ApiResponse.TaskInfo> overduedTasks = taskRepository
                .findOverdueTasksByAssignee(currentUser, LocalDate.now())
                .stream()
                .map(taskService::toTaskInfo)
                .collect(Collectors.toList());

        // Recent 5 projects
        List<ApiResponse.ProjectInfo> recentProjects = allProjects.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .collect(Collectors.toList());

        return ApiResponse.DashboardStats.builder()
                .totalProjects(allProjects.size())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .overdueTasks(overdueTasks)
                .recentProjects(recentProjects)
                .myTasks(myTasks)
                .overduedTasks(overduedTasks)
                .build();
    }
}
