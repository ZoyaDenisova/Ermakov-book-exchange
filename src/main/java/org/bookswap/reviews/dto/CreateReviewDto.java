package org.bookswap.reviews.dto;

public record CreateReviewDto(
        Long listingId,
        int rating,
        String comment
) {}

