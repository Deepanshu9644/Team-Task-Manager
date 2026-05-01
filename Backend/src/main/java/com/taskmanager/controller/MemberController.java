package com.taskmanager.controller;

import com.taskmanager.dto.request.MemberRequest;
import com.taskmanager.dto.response.ApiResponse;
import com.taskmanager.entity.User;
import com.taskmanager.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final CurrentUserResolver userResolver;

    @GetMapping
    public ResponseEntity<ApiResponse.Success<List<ApiResponse.MemberInfo>>> getMembers(
            @PathVariable Long projectId) {
        User currentUser = userResolver.resolve();
        List<ApiResponse.MemberInfo> members = memberService.getProjectMembers(projectId, currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of(members));
    }

    @PostMapping("/invite")
    public ResponseEntity<ApiResponse.Success<ApiResponse.MemberInfo>> inviteMember(
            @PathVariable Long projectId,
            @Valid @RequestBody MemberRequest.Invite request) {
        User currentUser = userResolver.resolve();
        ApiResponse.MemberInfo member = memberService.inviteMember(projectId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.Success.of("Member invited successfully", member));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse.Success<ApiResponse.MemberInfo>> updateRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @Valid @RequestBody MemberRequest.UpdateRole request) {
        User currentUser = userResolver.resolve();
        ApiResponse.MemberInfo member = memberService.updateMemberRole(projectId, userId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of("Member role updated", member));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse.Success<Void>> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        User currentUser = userResolver.resolve();
        memberService.removeMember(projectId, userId, currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of("Member removed successfully", null));
    }

    @DeleteMapping("/leave")
    public ResponseEntity<ApiResponse.Success<Void>> leaveProject(@PathVariable Long projectId) {
        User currentUser = userResolver.resolve();
        memberService.leaveProject(projectId, currentUser);
        return ResponseEntity.ok(ApiResponse.Success.of("You have left the project", null));
    }
}
