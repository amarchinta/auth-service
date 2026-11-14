package com.quizapp.auth_service.controller;

import com.quizapp.auth_service.dto.UserResponse;
import com.quizapp.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal String userId) {
        return userService.getById(UUID.fromString(userId));
    }
}