package org.bookswap.exchange.mapper;

import lombok.RequiredArgsConstructor;
import org.bookswap.exchange.dto.ExchangeDto;
import org.bookswap.exchange.entity.Exchange;
import org.bookswap.listings.mapper.*;
import org.bookswap.auth.mapper.UserMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangeMapper {

    private final UserMapper userMapper;
    private final ListingMapper listingMapper;

    public ExchangeDto toDto(Exchange e) {
        return new ExchangeDto(
                e.getId(),
                userMapper.toDto(e.getSender()),
                userMapper.toDto(e.getReceiver()),
                listingMapper.toDto(e.getOffered()),
                listingMapper.toDto(e.getSelected()),
                e.getStatus(),
                e.isSenderConfirmedCompletion(),
                e.isReceiverConfirmedCompletion(),
                e.getCreatedAt(),
                e.getCompletedAt()
        );
    }
}
