package org.bookswap.catalog.dto;

import java.time.LocalDateTime;

public record WantedBookDto(
        Long id,
        Long bookId,
        Long userId,
        LocalDateTime createdAt
) {}
