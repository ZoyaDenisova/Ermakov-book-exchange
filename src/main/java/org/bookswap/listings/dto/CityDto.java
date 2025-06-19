package org.bookswap.listings.dto;

public record CityDto(
        Long id,
        String name,
        String region,
        String country
) {}