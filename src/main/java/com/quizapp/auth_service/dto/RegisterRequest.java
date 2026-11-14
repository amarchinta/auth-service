package com.quizapp.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Full name is required") @Size(max = 100, message = "Full name must be at most 100 characters") String fullName,
        @NotBlank(message = "Email format is invalid") @Email(message = "Email format is invalid") @Size(max = 100, message = "Email must be at most 100 characters") String email,
        @NotBlank(message = "Password is required") @Size(min = 8, max = 64, message = "Password must be 8 to 64 characters") String password) {
}
