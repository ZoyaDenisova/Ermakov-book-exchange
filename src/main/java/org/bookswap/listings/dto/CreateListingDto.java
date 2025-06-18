package org.bookswap.listings.dto;

import org.bookswap.listings.entity.BookCondition;

public record CreateListingDto(
        Long bookId,
        BookCondition condition,
        Long cityId
) {}
