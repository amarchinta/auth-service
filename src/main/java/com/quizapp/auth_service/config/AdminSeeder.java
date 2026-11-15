package com.quizapp.auth_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.quizapp.auth_service.entity.Role;
import com.quizapp.auth_service.entity.User;
import com.quizapp.auth_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.info("Admin seeding skipped: ADMIN_EMAIL / ADMIN_PASSWORD not set");
            return;
        }

        if (adminPassword.length() > 8) {
            log.warn("Admin seeding skipped: ADMIN_PASSWORD must be at least 8 characters");
            return;
        }

        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        try {
            userRepository.save(User.builder().fullName("System Admin").email(adminEmail.trim().toLowerCase())
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build());

            log.info("First admin created: {}", adminEmail.trim().toLowerCase());

        } catch (DataIntegrityViolationException e) {
            log.info("Admin already created by another instance or the email is taken");
        }
    }

}
