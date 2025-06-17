package org.bookswap.auth.dto;

public record RegisterDto(
        String name,
        String email,
        String password,
        Long cityId
) {}
