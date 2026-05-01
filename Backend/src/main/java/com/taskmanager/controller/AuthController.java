package com.taskmanager.controller;

import com.taskmanager.dto.request.AuthRequest;
import com.taskmanager.dto.response.ApiResponse;
import com.taskmanager.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse.Success<ApiResponse.AuthToken>> register(
            @Valid @RequestBody AuthRequest.Register request) {
        ApiResponse.AuthToken token = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.Success.of("Registration successful", token));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse.Success<ApiResponse.AuthToken>> login(
            @Valid @RequestBody AuthRequest.Login request) {
        ApiResponse.AuthToken token = authService.login(request);
        return ResponseEntity.ok(ApiResponse.Success.of("Login successful", token));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Task Manager API is running ✅");
    }
}
