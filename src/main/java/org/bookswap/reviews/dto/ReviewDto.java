package org.bookswap.reviews.dto;

import org.bookswap.auth.dto.UserDto;
import org.bookswap.catalog.entity.ModerationStatus;
import org.bookswap.listings.dto.ListingDto;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewDto(
        Long id,
        ListingDto listing,
        UserDto fromUser,
        UserDto toUser,
        int rating,
        String comment,
        ModerationStatus moderationStatus,
        List<String> imageUrls,
        LocalDateTime createdAt
) {}



