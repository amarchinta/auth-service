package com.quizapp.auth_service.dto;

import java.util.UUID;

import com.quizapp.auth_service.entity.Role;
import com.quizapp.auth_service.entity.User;

public record UserResponse(
        UUID id,
        String fullName,
        String email,
        Role role) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }

}
