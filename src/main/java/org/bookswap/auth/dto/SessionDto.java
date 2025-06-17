package org.bookswap.auth.dto;

public record SessionDto(
        Long id,
        String refreshToken,
        String createdAt,
        String expiresAt
) {}
