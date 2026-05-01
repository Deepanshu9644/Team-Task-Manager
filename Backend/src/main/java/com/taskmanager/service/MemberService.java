package com.taskmanager.service;

import com.taskmanager.dto.request.MemberRequest;
import com.taskmanager.dto.response.ApiResponse;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.ProjectMember;
import com.taskmanager.entity.User;
import com.taskmanager.exception.BadRequestException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.ProjectMemberRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectService projectService;

    // ─── Get Members ─────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ApiResponse.MemberInfo> getProjectMembers(Long projectId, User currentUser) {
        Project project = projectService.getProjectWithAccessCheck(projectId, currentUser);
        return memberRepository.findByProject(project)
                .stream()
                .map(this::toMemberInfo)
                .collect(Collectors.toList());
    }

    // ─── Invite Member ────────────────────────────────────────────────
    @Transactional
    public ApiResponse.MemberInfo inviteMember(Long projectId, MemberRequest.Invite request, User currentUser) {
        Project project = projectService.getProjectWithAccessCheck(projectId, currentUser);
        projectService.requireProjectAdmin(projectId, currentUser);

        User invitee = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (memberRepository.existsByProjectAndUser(project, invitee)) {
            throw new BadRequestException("User is already a member of this project");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(invitee)
                .role(request.getRole())
                .build();

        member = memberRepository.save(member);
        return toMemberInfo(member);
    }

    // ─── Update Member Role ────────────────────────────────────────────
    @Transactional
    public ApiResponse.MemberInfo updateMemberRole(Long projectId, Long userId,
                                                    MemberRequest.UpdateRole request, User currentUser) {
        projectService.getProjectWithAccessCheck(projectId, currentUser);
        projectService.requireProjectAdmin(projectId, currentUser);

        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this project"));

        if (member.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You cannot change your own role");
        }

        member.setRole(request.getRole());
        member = memberRepository.save(member);
        return toMemberInfo(member);
    }

    // ─── Remove Member ─────────────────────────────────────────────────
    @Transactional
    public void removeMember(Long projectId, Long userId, User currentUser) {
        projectService.getProjectWithAccessCheck(projectId, currentUser);
        projectService.requireProjectAdmin(projectId, currentUser);

        if (userId.equals(currentUser.getId())) {
            throw new BadRequestException("You cannot remove yourself from the project");
        }

        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this project"));

        memberRepository.delete(member);
    }

    // ─── Leave Project ─────────────────────────────────────────────────
    @Transactional
    public void leaveProject(Long projectId, User currentUser) {
        Project project = projectService.getProjectWithAccessCheck(projectId, currentUser);

        if (project.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Project creator cannot leave the project. Transfer ownership or delete it.");
        }

        ProjectMember member = memberRepository.findByProjectAndUser(project, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this project"));

        memberRepository.delete(member);
    }

    // ─── Mapper ────────────────────────────────────────────────────────
    private ApiResponse.MemberInfo toMemberInfo(ProjectMember pm) {
        return ApiResponse.MemberInfo.builder()
                .memberId(pm.getId())
                .user(projectService.toUserInfo(pm.getUser()))
                .role(pm.getRole())
                .joinedAt(pm.getJoinedAt())
                .tasksAssigned(taskRepository.countByAssignee(pm.getUser()))
                .build();
    }
}
