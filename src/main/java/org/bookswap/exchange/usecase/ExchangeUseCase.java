package org.bookswap.exchange.usecase;

import lombok.RequiredArgsConstructor;
import org.bookswap.auth.repository.UserRepo;
import org.bookswap.auth.security.SecurityUtil;
import org.bookswap.common.exception.BadRequestException;
import org.bookswap.common.exception.ConflictException;
import org.bookswap.common.exception.ForbiddenException;
import org.bookswap.common.exception.NotFoundException;
import org.bookswap.exchange.dto.ExchangeCreateDto;
import org.bookswap.exchange.dto.ExchangeDto;
import org.bookswap.exchange.dto.ExchangeFilterDto;
import org.bookswap.exchange.entity.Exchange;
import org.bookswap.exchange.entity.ExchangeStatus;
import org.bookswap.exchange.repository.ExchangeRepo;
import org.bookswap.listings.entity.Listing;
import org.bookswap.listings.repository.ListingRepo;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExchangeUseCase {

    private final ExchangeRepo exchangeRepo;
    private final ListingRepo listingRepo;
    private final UserRepo userRepo;

    @Transactional
    public ExchangeDto proposeExchange(Long senderId, ExchangeCreateDto dto) {
        if (dto.offeredListingId().equals(dto.selectedListingId())) {
            throw new BadRequestException("Cannot exchange listing with itself");
        }

        Listing offered = listingRepo.findById(dto.offeredListingId())
                .orElseThrow(() -> new NotFoundException("Offered listing not found"));
        Listing selected = listingRepo.findById(dto.selectedListingId())
                .orElseThrow(() -> new NotFoundException("Selected listing not found"));

        if (!offered.getOwner().getId().equals(senderId)) {
            throw new ForbiddenException("You do not own the offered listing");
        }

        if (!offered.isOpen() || !selected.isOpen()) {
            throw new BadRequestException("Both listings must be open");
        }

        if (exchangeRepo.isListingInPendingExchange(offered.getId()) ||
                exchangeRepo.isListingInPendingExchange(selected.getId())) {
            throw new ConflictException("One of the listings is already in a pending exchange");
        }

        if (exchangeRepo.findByOfferedIdAndSelectedId(offered.getId(), selected.getId()).isPresent()) {
            throw new ConflictException("Duplicate exchange proposal");
        }

        Exchange exchange = Exchange.builder()
                .sender(offered.getOwner())
                .receiver(selected.getOwner())
                .offered(offered)
                .selected(selected)
                .status(ExchangeStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return toDto(exchangeRepo.save(exchange));
    }

    @Transactional
    public void approveExchange(Long exchangeId, Long userId) {
        Exchange exchange = getExchangeOrThrow(exchangeId);
        SecurityUtil.assertIsSelfOrThrow(userId, exchange.getReceiver().getId());

        if (exchange.getStatus() != ExchangeStatus.PENDING) {
            throw new BadRequestException("Exchange already resolved");
        }

        exchange.setStatus(ExchangeStatus.APPROVED);

        listingRepo.closeListing(exchange.getOffered().getId());
        listingRepo.closeListing(exchange.getSelected().getId());
    }

    @Transactional
    public void rejectExchange(Long exchangeId, Long userId) {
        Exchange exchange = getExchangeOrThrow(exchangeId);
        SecurityUtil.assertIsSelfOrThrow(userId, exchange.getReceiver().getId());

        if (exchange.getStatus() != ExchangeStatus.PENDING) {
            throw new BadRequestException("Exchange already resolved");
        }

        exchange.setStatus(ExchangeStatus.REJECTED);
    }

    @Transactional
    public void confirmCompletion(Long exchangeId, Long userId) {
        Exchange exchange = exchangeRepo.findById(exchangeId)
                .orElseThrow(() -> new NotFoundException("Exchange not found"));

        if (exchange.getStatus() != ExchangeStatus.APPROVED) {
            throw new BadRequestException("Exchange is not approved");
        }

        boolean isSender   = exchange.getSender().getId().equals(userId);
        boolean isReceiver = exchange.getReceiver().getId().equals(userId);

        if (!isSender && !isReceiver) {
            throw new ForbiddenException("You are not part of this exchange");
        }

        if (isSender) {
            if (exchange.isSenderConfirmedCompletion()) {
                throw new ConflictException("Already confirmed by sender");
            }
            exchange.setSenderConfirmedCompletion(true);
        } else {
            if (exchange.isReceiverConfirmedCompletion()) {
                throw new ConflictException("Already confirmed by receiver");
            }
            exchange.setReceiverConfirmedCompletion(true);
        }

        if (exchange.isSenderConfirmedCompletion() && exchange.isReceiverConfirmedCompletion()) {
            exchange.setCompletedAt(LocalDateTime.now());
        }
    }

    @Transactional(readOnly = true)
    public Page<ExchangeDto> getUserExchanges(Long userId, ExchangeFilterDto filter, Pageable pageable) {
        ExchangeStatus status = (filter != null) ? filter.status() : null;

        Page<Exchange> page = exchangeRepo.findAllByUserInvolvedAndOptionalStatus(userId, status, pageable);
        return page.map(this::toDto);
    }

    private Exchange getExchangeOrThrow(Long id) {
        return exchangeRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Exchange not found"));
    }


    private ExchangeDto toDto(Exchange e) {
        return new ExchangeDto(
                e.getId(),
                e.getSender().getId(),
                e.getReceiver().getId(),
                e.getOffered().getId(),
                e.getSelected().getId(),
                e.getStatus(),
                e.isSenderConfirmedCompletion(),
                e.isReceiverConfirmedCompletion(),
                e.getCreatedAt(),
                e.getCompletedAt()
        );
    }
}
