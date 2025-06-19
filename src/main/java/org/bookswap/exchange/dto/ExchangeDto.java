package org.bookswap.exchange.dto;

import org.bookswap.exchange.entity.ExchangeStatus;

import java.time.LocalDateTime;

public record ExchangeDto(
        Long id,
        Long senderId,
        Long receiverId,
        Long offeredListingId,
        Long selectedListingId,
        ExchangeStatus status,
        boolean senderConfirmedCompletion,
        boolean receiverConfirmedCompletion,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {}
