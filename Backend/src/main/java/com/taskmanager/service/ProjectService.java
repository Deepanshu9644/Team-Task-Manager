package com.taskmanager.service;

import com.taskmanager.dto.request.ProjectRequest;
import com.taskmanager.dto.response.ApiResponse;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.ProjectMember;
import com.taskmanager.entity.User;
import com.taskmanager.enums.Role;
import com.taskmanager.exception.AccessDeniedException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.ProjectMemberRepository;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final TaskRepository taskRepository;

    // ─── Create ──────────────────────────────────────────────────────
    @Transactional
    public ApiResponse.ProjectInfo createProject(ProjectRequest.Create request, User currentUser) {
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .color(request.getColor())
                .dueDate(request.getDueDate())
                .createdBy(currentUser)
                .build();

        project = projectRepository.save(project);

        // Creator is automatically an ADMIN member
        ProjectMember adminMember = ProjectMember.builder()
                .project(project)
                .user(currentUser)
                .role(Role.ADMIN)
                .build();
        memberRepository.save(adminMember);

        return toProjectInfo(project);
    }

    // ─── Get All Projects for User ────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ApiResponse.ProjectInfo> getAllProjects(User currentUser) {
        return projectRepository.findAllByUserMembership(currentUser)
                .stream()
                .map(this::toProjectInfo)
                .collect(Collectors.toList());
    }

    // ─── Get Project Detail ────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ApiResponse.ProjectDetail getProjectDetail(Long projectId, User currentUser) {
        Project project = getProjectWithAccessCheck(projectId, currentUser);
        return toProjectDetail(project);
    }

    // ─── Update Project ────────────────────────────────────────────────
    @Transactional
    public ApiResponse.ProjectInfo updateProject(Long projectId, ProjectRequest.Update request, User currentUser) {
        Project project = getProjectWithAccessCheck(projectId, currentUser);
        requireProjectAdmin(projectId, currentUser);

        if (request.getName() != null) project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getColor() != null) project.setColor(request.getColor());
        if (request.getDueDate() != null) project.setDueDate(request.getDueDate());
        if (request.getStatus() != null) project.setStatus(request.getStatus());

        project = projectRepository.save(project);
        return toProjectInfo(project);
    }

    // ─── Delete Project ────────────────────────────────────────────────
    @Transactional
    public void deleteProject(Long projectId, User currentUser) {
        Project project = getProjectWithAccessCheck(projectId, currentUser);
        requireProjectAdmin(projectId, currentUser);
        projectRepository.delete(project);
    }

    // ─── Helpers ───────────────────────────────────────────────────────
    public Project getProjectWithAccessCheck(Long projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        boolean isMember = memberRepository.existsByProjectAndUser(project, currentUser);
        boolean isCreator = project.getCreatedBy().getId().equals(currentUser.getId());

        if (!isMember && !isCreator) {
            throw new AccessDeniedException("You are not a member of this project");
        }
        return project;
    }

    public void requireProjectAdmin(Long projectId, User currentUser) {
        boolean isAdmin = memberRepository.existsByProjectIdAndUserIdAndRole(
                projectId, currentUser.getId(), Role.ADMIN);
        if (!isAdmin) {
            throw new AccessDeniedException("Only project admins can perform this action");
        }
    }

    // ─── Mappers ────────────────────────────────────────────────────────
    public ApiResponse.UserInfo toUserInfo(User user) {
        return ApiResponse.UserInfo.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public ApiResponse.ProjectInfo toProjectInfo(Project project) {
        long total = taskRepository.countByProjectId(project.getId());
        long completed = taskRepository.countCompletedByProjectId(project.getId());
        int progress = total == 0 ? 0 : (int) ((completed * 100) / total);

        return ApiResponse.ProjectInfo.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .color(project.getColor())
                .dueDate(project.getDueDate())
                .createdBy(toUserInfo(project.getCreatedBy()))
                .memberCount(project.getMembers().size())
                .totalTasks(total)
                .completedTasks(completed)
                .progressPercent(progress)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    private ApiResponse.ProjectDetail toProjectDetail(Project project) {
        long total = taskRepository.countByProjectId(project.getId());
        long completed = taskRepository.countCompletedByProjectId(project.getId());
        int progress = total == 0 ? 0 : (int) ((completed * 100) / total);

        List<ApiResponse.MemberInfo> members = project.getMembers().stream()
                .map(pm -> ApiResponse.MemberInfo.builder()
                        .memberId(pm.getId())
                        .user(toUserInfo(pm.getUser()))
                        .role(pm.getRole())
                        .joinedAt(pm.getJoinedAt())
                        .tasksAssigned(taskRepository.countByAssignee(pm.getUser()))
                        .build())
                .collect(Collectors.toList());

        List<ApiResponse.TaskInfo> tasks = project.getTasks().stream()
                .map(task -> {
                    boolean overdue = task.getDueDate() != null
                            && task.getDueDate().isBefore(java.time.LocalDate.now())
                            && task.getStatus() != com.taskmanager.enums.TaskStatus.DONE;
                    return ApiResponse.TaskInfo.builder()
                            .id(task.getId())
                            .title(task.getTitle())
                            .description(task.getDescription())
                            .status(task.getStatus())
                            .priority(task.getPriority())
                            .dueDate(task.getDueDate())
                            .overdue(overdue)
                            .projectId(project.getId())
                            .projectName(project.getName())
                            .assignee(task.getAssignee() != null ? toUserInfo(task.getAssignee()) : null)
                            .createdBy(toUserInfo(task.getCreatedBy()))
                            .createdAt(task.getCreatedAt())
                            .updatedAt(task.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return ApiResponse.ProjectDetail.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .color(project.getColor())
                .dueDate(project.getDueDate())
                .createdBy(toUserInfo(project.getCreatedBy()))
                .members(members)
                .tasks(tasks)
                .totalTasks(total)
                .completedTasks(completed)
                .progressPercent(progress)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
