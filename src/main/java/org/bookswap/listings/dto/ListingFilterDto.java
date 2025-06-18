package org.bookswap.listings.dto;

import org.bookswap.catalog.entity.AgeCategory;
import org.bookswap.listings.entity.BookCondition;

import java.util.List;

public record ListingFilterDto(
        String title,
        String author,
        List<AgeCategory> ageCategories,
        List<Long> genreIds,
        Long cityId,
        BookCondition condition
) {}

