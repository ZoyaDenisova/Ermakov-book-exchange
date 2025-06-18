package org.bookswap.catalog.dto;

import org.bookswap.catalog.entity.AgeCategory;

import java.util.List;

public record BookFilterDto(
        String title,
        String author,
        List<AgeCategory> ageCategories,
        List<Long> genreIds
) {}
