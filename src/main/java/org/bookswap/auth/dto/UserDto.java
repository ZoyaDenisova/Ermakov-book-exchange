package org.bookswap.auth.dto;

public record UserDto(
        Long id,
        String name,
        String email,
        String avatarUrl,
        String role,
        boolean isBanned,
        Long cityId
) {}
