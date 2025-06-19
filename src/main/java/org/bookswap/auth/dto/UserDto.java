package org.bookswap.auth.dto;

import org.bookswap.listings.dto.CityDto;

public record UserDto(
        Long id,
        String name,
        String email,
        String avatarUrl,
        String role,
        boolean isBanned,
        CityDto city
) {}

