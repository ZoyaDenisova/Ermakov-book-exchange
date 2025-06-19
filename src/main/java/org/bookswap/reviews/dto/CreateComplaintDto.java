package org.bookswap.reviews.dto;

public record CreateComplaintDto(
        Long listingId,
        String comment
) {}
