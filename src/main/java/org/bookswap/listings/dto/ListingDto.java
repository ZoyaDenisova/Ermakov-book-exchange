package org.bookswap.listings.dto;

import org.bookswap.listings.entity.BookCondition;

import java.time.LocalDateTime;
import java.util.List;

public record ListingDto(
        Long id,
        Long bookId,
        String bookTitle,
        String bookAuthor,
        String bookDescription,
        BookCondition condition,
        Long cityId,
        String cityName,
        List<String> imageUrls,
        boolean isOpen,
        boolean isBlocked,
        Long ownerId,
        String ownerName,
        LocalDateTime createdAt
) {}



