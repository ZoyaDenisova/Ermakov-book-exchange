package org.bookswap.reviews.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ComplaintDto(
        Long id,
        Long listingId,
        Long fromUserId,
        Long toUserId,
        String comment,
        boolean isReviewed,
        List<String> imageUrls,
        LocalDateTime createdAt
) {}
