package com.taskmanager.service;

import com.taskmanager.dto.response.ApiResponse;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProjectService projectService;

    @Transactional(readOnly = true)
    public ApiResponse.UserInfo getProfile(User currentUser) {
        return projectService.toUserInfo(currentUser);
    }

    @Transactional(readOnly = true)
    public List<ApiResponse.UserInfo> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(projectService::toUserInfo)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApiResponse.UserInfo getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return projectService.toUserInfo(user);
    }
}
