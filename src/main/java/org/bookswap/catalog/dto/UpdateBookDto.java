package org.bookswap.catalog.dto;

import org.bookswap.catalog.entity.AgeCategory;

import java.util.List;

public record UpdateBookDto(
        String title,
        String author,
        Integer year,
        String description,
        List<Long> genreIds,
        AgeCategory ageCategory
) {}
