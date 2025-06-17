package org.bookswap.auth.dto;

public record BookShortDto(
        Long id,
        String title,
        String author
) {}