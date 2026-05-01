package com.taskmanager.service;

import com.taskmanager.dto.request.TaskRequest;
import com.taskmanager.dto.response.ApiResponse;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.exception.AccessDeniedException;
import com.taskmanager.exception.BadRequestException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.ProjectMemberRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectService projectService;

    // ─── Create Task ─────────────────────────────────────────────────
    @Transactional
    public ApiResponse.TaskInfo createTask(Long projectId, TaskRequest.Create request, User currentUser) {
        Project project = projectService.getProjectWithAccessCheck(projectId, currentUser);

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssigneeId()));

            boolean assigneeIsMember = memberRepository.existsByProjectAndUser(project, assignee);
            if (!assigneeIsMember) {
                throw new BadRequestException("Assignee is not a member of this project");
            }
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : com.taskmanager.enums.TaskPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .project(project)
                .assignee(assignee)
                .createdBy(currentUser)
                .build();

        task = taskRepository.save(task);
        return toTaskInfo(task);
    }

    // ─── Get All Tasks in a Project ───────────────────────────────────
    @Transactional(readOnly = true)
    public List<ApiResponse.TaskInfo> getProjectTasks(Long projectId, User currentUser) {
        projectService.getProjectWithAccessCheck(projectId, currentUser);
        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(this::toTaskInfo)
                .collect(Collectors.toList());
    }

    // ─── Get Task By ID ────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ApiResponse.TaskInfo getTask(Long projectId, Long taskId, User currentUser) {
        projectService.getProjectWithAccessCheck(projectId, currentUser);
        Task task = getTaskAndVerifyProject(taskId, projectId);
        return toTaskInfo(task);
    }

    // ─── Update Task ───────────────────────────────────────────────────
    @Transactional
    public ApiResponse.TaskInfo updateTask(Long projectId, Long taskId,
                                           TaskRequest.Update request, User currentUser) {
        Project project = projectService.getProjectWithAccessCheck(projectId, currentUser);
        Task task = getTaskAndVerifyProject(taskId, projectId);

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssigneeId()));
            boolean isMember = memberRepository.existsByProjectAndUser(project, assignee);
            if (!isMember) {
                throw new BadRequestException("Assignee is not a member of this project");
            }
            task.setAssignee(assignee);
        }

        task = taskRepository.save(task);
        return toTaskInfo(task);
    }

    // ─── Delete Task ───────────────────────────────────────────────────
    @Transactional
    public void deleteTask(Long projectId, Long taskId, User currentUser) {
        projectService.getProjectWithAccessCheck(projectId, currentUser);
        projectService.requireProjectAdmin(projectId, currentUser);
        Task task = getTaskAndVerifyProject(taskId, projectId);
        taskRepository.delete(task);
    }

    // ─── Update Status Only ────────────────────────────────────────────
    @Transactional
    public ApiResponse.TaskInfo updateTaskStatus(Long projectId, Long taskId,
                                                  TaskStatus status, User currentUser) {
        projectService.getProjectWithAccessCheck(projectId, currentUser);
        Task task = getTaskAndVerifyProject(taskId, projectId);
        task.setStatus(status);
        task = taskRepository.save(task);
        return toTaskInfo(task);
    }

    // ─── My Tasks ─────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ApiResponse.TaskInfo> getMyTasks(User currentUser) {
        return taskRepository.findByAssignee(currentUser)
                .stream()
                .map(this::toTaskInfo)
                .collect(Collectors.toList());
    }

    // ─── Overdue Tasks ─────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ApiResponse.TaskInfo> getMyOverdueTasks(User currentUser) {
        return taskRepository.findOverdueTasksByAssignee(currentUser, LocalDate.now())
                .stream()
                .map(this::toTaskInfo)
                .collect(Collectors.toList());
    }

    // ─── Helpers ───────────────────────────────────────────────────────
    private Task getTaskAndVerifyProject(Long taskId, Long projectId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        if (!task.getProject().getId().equals(projectId)) {
            throw new AccessDeniedException("Task does not belong to this project");
        }
        return task;
    }

    public ApiResponse.TaskInfo toTaskInfo(Task task) {
        boolean overdue = task.getDueDate() != null
                && task.getDueDate().isBefore(LocalDate.now())
                && task.getStatus() != TaskStatus.DONE;

        return ApiResponse.TaskInfo.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .overdue(overdue)
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .assignee(task.getAssignee() != null ? projectService.toUserInfo(task.getAssignee()) : null)
                .createdBy(projectService.toUserInfo(task.getCreatedBy()))
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
