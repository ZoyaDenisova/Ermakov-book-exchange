package org.bookswap.reviews.dto;

import org.bookswap.auth.dto.UserDto;
import org.bookswap.listings.dto.ListingDto;

import java.time.LocalDateTime;
import java.util.List;

public record ComplaintDto(
        Long id,
        ListingDto listing,
        UserDto fromUser,
        UserDto toUser,
        String comment,
        boolean isReviewed,
        List<String> imageUrls,
        LocalDateTime createdAt
) {}

