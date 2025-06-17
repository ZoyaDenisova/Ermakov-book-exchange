package org.bookswap.auth.dto;

import java.util.List;

public record UserPublicDto(
        Long id,
        String name,
        String avatarUrl,
        String cityName,
        List<BookShortDto> canOffer,
        List<BookShortDto> wants
) {}