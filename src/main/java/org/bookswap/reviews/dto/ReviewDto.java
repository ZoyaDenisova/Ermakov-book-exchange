package org.bookswap.reviews.dto;

import org.bookswap.catalog.entity.ModerationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewDto(
        Long id,
        Long listingId,
        Long fromUserId,
        Long toUserId,
        int rating,
        String comment,
        ModerationStatus moderationStatus,
        List<String> imageUrls,
        LocalDateTime createdAt
) {}
