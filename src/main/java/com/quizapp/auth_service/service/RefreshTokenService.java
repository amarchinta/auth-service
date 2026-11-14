package com.quizapp.auth_service.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quizapp.auth_service.entity.RefreshToken;
import com.quizapp.auth_service.exception.InvalidRefreshTokenException;
import com.quizapp.auth_service.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(noRollbackFor = InvalidRefreshTokenException.class)
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository repository;

    @Value("${app.jwt.refresh-expiration-days}")
    private long expirationDays;

    /**
     * Creates a token, saves only its hash, returns the raw value (the client's
     * copy).
     */
    public String create(UUID userId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setTokenHash(hash(raw));
        token.setExpiresAt(Instant.now().plus(Duration.ofDays(expirationDays)));
        repository.save(token);

        return raw;
    }

    /** Validates the token, marks it used (rotation), returns the user id. */
    public UUID consume(String raw) {
        RefreshToken token = repository.findByTokenHash(hash(raw))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (token.isRevoked()) {
            // A used token came back: possible theft. Revoke everything for this user.
            repository.revokeAllByUserId(token.getUserId());
            throw new InvalidRefreshTokenException();
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }

        token.setRevoked(true);
        return token.getUserId();
    }

    /** Logout: revoke this token if it exists. Unknown tokens are ignored. */
    public void revoke(String raw) {
        repository.findByTokenHash(hash(raw)).ifPresent(t -> t.setRevoked(true));
    }

    private static String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}