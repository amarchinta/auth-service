package com.quizapp.auth_service.dto;

public record LoginResponse(
                String accessToken,
                String refreshToken,
                String tokenType,
                long expiresIn) {
}
