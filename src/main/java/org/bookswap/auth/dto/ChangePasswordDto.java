package org.bookswap.auth.dto;

public record ChangePasswordDto(
        String oldPassword,
        String newPassword
) {}
