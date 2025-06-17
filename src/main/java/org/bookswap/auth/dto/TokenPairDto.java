package org.bookswap.auth.dto;

import java.time.LocalDateTime;

public record TokenPairDto(
        String accessToken,
        String refreshToken,
        LocalDateTime accessExpires,
        LocalDateTime refreshExpires
) {}

