package org.bookswap.reviews.dto;

import org.bookswap.catalog.entity.ModerationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewDto(
        Long id,
        Long listingId,
        Long fromUserId,
        String fromUserName,
        String fromUserAvatar,
        Long toUserId,
        int rating,
        String comment,
        ModerationStatus moderationStatus,
        List<String> imageUrls,
        String bookTitle,
        String bookAuthor,
        String bookImageUrl,
        LocalDateTime createdAt
) {}


