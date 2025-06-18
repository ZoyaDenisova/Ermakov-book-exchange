package org.bookswap.catalog.dto;

import org.bookswap.catalog.entity.AgeCategory;
import org.bookswap.catalog.entity.ModerationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record BookDto(
        Long id,
        String title,
        String author,
        Integer year,
        String description,
        List<String> genres,
        AgeCategory ageCategory,
        String imageUrl,
        ModerationStatus moderationStatus,
        Long createdById,
        LocalDateTime createdAt
) {}
