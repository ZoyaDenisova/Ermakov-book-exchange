package org.bookswap.exchange.dto;

import org.bookswap.auth.dto.UserDto;
import org.bookswap.exchange.entity.ExchangeStatus;
import org.bookswap.listings.dto.ListingDto;

import java.time.LocalDateTime;

public record ExchangeDto(
        Long id,
        UserDto sender,
        UserDto receiver,
        ListingDto offeredListing,
        ListingDto selectedListing,
        ExchangeStatus status,
        boolean senderConfirmedCompletion,
        boolean receiverConfirmedCompletion,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {}

