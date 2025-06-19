package org.bookswap.messaging.dto;

import java.time.LocalDateTime;
public record DialogDto(
        Long dialogId,
        Long listingId,
        String bookTitle,
        String bookAuthor,
        String bookImageUrl,
        String bookCondition,
        Long listingOwnerId,
        String listingOwnerName,
        String listingOwnerAvatar,
        String lastMessageContent,
        String lastMessageAuthor,
        LocalDateTime lastMessageTime
) {}