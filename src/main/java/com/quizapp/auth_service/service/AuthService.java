package com.quizapp.auth_service.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quizapp.auth_service.dto.LoginRequest;
import com.quizapp.auth_service.dto.LoginResponse;
import com.quizapp.auth_service.dto.RefreshRequest;
import com.quizapp.auth_service.dto.RegisterRequest;
import com.quizapp.auth_service.dto.UserResponse;
import com.quizapp.auth_service.entity.Role;
import com.quizapp.auth_service.entity.User;
import com.quizapp.auth_service.exception.EmailAlreadyExistsException;
import com.quizapp.auth_service.exception.InvalidCredentialsException;
import com.quizapp.auth_service.exception.InvalidRefreshTokenException;
import com.quizapp.auth_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = User.builder().fullName(request.fullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return issueTokens(user);
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public LoginResponse refresh(RefreshRequest request) {
        UUID userId = refreshTokenService.consume(request.refreshToken());

        User user = userRepository.findById(userId)
                .filter(User::isEnabled)
                .orElseThrow(InvalidRefreshTokenException::new);

        return issueTokens(user);
    }

    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    private LoginResponse issueTokens(User user) {
        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.create(user.getId());
        return new LoginResponse(accessToken, refreshToken, "Bearer", jwtService.getExpirationSeconds());
    }
}
