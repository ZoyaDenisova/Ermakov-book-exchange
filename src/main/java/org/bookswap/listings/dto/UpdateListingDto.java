package org.bookswap.listings.dto;

import org.bookswap.listings.entity.BookCondition;

public record UpdateListingDto(
        BookCondition condition,
        Long cityId
) {}
