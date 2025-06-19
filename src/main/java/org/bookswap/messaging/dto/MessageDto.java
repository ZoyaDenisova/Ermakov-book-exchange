package org.bookswap.messaging.dto;

import org.bookswap.exchange.dto.ExchangeDto;

import java.time.LocalDateTime;
import java.util.List;

public record MessageDto(
        Long messageId,
        Long authorId,
        String authorName,
        String content,
        List<String> imageUrls,
        ExchangeDto exchange,
        boolean isExchangeProposal,
        LocalDateTime createdAt
) {}
