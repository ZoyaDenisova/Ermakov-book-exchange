package org.bookswap.auth.dto;

public record UpdateUserDto(
        String name,
        String avatarUrl,
        Long cityId
) {}
