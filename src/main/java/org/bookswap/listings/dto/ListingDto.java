package org.bookswap.listings.dto;

import org.bookswap.auth.dto.UserDto;
import org.bookswap.catalog.dto.BookDto;
import org.bookswap.listings.entity.BookCondition;

import java.time.LocalDateTime;
import java.util.List;

public record ListingDto(
        Long id,
        BookDto book,
        CityDto city,
        UserDto owner,
        BookCondition condition,
        List<String> imageUrls,
        boolean isOpen,
        boolean isBlocked,
        LocalDateTime createdAt
) {}




