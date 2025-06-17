package org.bookswap.auth.dto;

public record LoginDto(
        String email,
        String password
) {}
