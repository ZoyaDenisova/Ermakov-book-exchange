package org.bookswap.exchange.usecase;

import lombok.RequiredArgsConstructor;
import org.bookswap.auth.dto.UserDto;
import org.bookswap.auth.entity.User;
import org.bookswap.auth.security.SecurityUtil;
import org.bookswap.catalog.dto.BookDto;
import org.bookswap.catalog.entity.Book;
import org.bookswap.catalog.entity.BookImage;
import org.bookswap.catalog.entity.Genre;
import org.bookswap.catalog.repository.BookImageRepo;
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
import org.bookswap.listings.dto.CityDto;
import org.bookswap.listings.dto.ListingDto;
import org.bookswap.listings.entity.City;
import org.bookswap.listings.entity.Listing;
import org.bookswap.listings.entity.ListingImage;
import org.bookswap.listings.repository.ListingImageRepo;
import org.bookswap.listings.repository.ListingRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeUseCase {

    private final ExchangeRepo exchangeRepo;
    private final ListingRepo listingRepo;
    private final ListingImageRepo listingImageRepo;
    private final BookImageRepo bookImageRepo;

    @Transactional
    public ExchangeDto proposeExchange(Long senderId, ExchangeCreateDto dto) {
        Exchange exchange = proposeExchangeEntity(senderId, dto);
        return toDto(exchange);
    }

    public Exchange proposeExchangeEntity(Long senderId, ExchangeCreateDto dto) {
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

        return exchangeRepo.save(exchange);
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


    public ExchangeDto toDto(Exchange e) {
        User sender = e.getSender();
        User receiver = e.getReceiver();
        Listing offered = e.getOffered();
        Listing selected = e.getSelected();

        return new ExchangeDto(
                e.getId(),
                new UserDto(
                        sender.getId(),
                        sender.getName(),
                        sender.getEmail(),
                        sender.getAvatarUrl(),
                        sender.getRole().name(),
                        sender.isBanned(),
                        new CityDto(
                                sender.getCity().getId(),
                                sender.getCity().getName(),
                                sender.getCity().getRegion(),
                                sender.getCity().getCountry()
                        )
                ),
                new UserDto(
                        receiver.getId(),
                        receiver.getName(),
                        receiver.getEmail(),
                        receiver.getAvatarUrl(),
                        receiver.getRole().name(),
                        receiver.isBanned(),
                        new CityDto(
                                receiver.getCity().getId(),
                                receiver.getCity().getName(),
                                receiver.getCity().getRegion(),
                                receiver.getCity().getCountry()
                        )
                ),
                toListingDto(offered),
                toListingDto(selected),
                e.getStatus(),
                e.isSenderConfirmedCompletion(),
                e.isReceiverConfirmedCompletion(),
                e.getCreatedAt(),
                e.getCompletedAt()
        );
    }
    private ListingDto toListingDto(Listing listing) {
        Book book = listing.getBook();
        User owner = listing.getOwner();
        City city = listing.getCity();
        City ownerCity = owner.getCity();

        List<String> imageUrls = listingImageRepo.findByListingId(listing.getId()).stream()
                .map(ListingImage::getUrl)
                .toList();

        String bookImageUrl = bookImageRepo.findByBookId(book.getId()).stream()
                .map(BookImage::getUrl)
                .findFirst()
                .orElse(null);

        return new ListingDto(
                listing.getId(),
                new BookDto(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getYear(),
                        book.getDescription(),
                        book.getGenres().stream().map(Genre::getName).toList(),
                        book.getAgeCategory(),
                        bookImageUrl,
                        book.getModerationStatus(),
                        book.getCreatedBy() != null ? book.getCreatedBy().getId() : null,
                        book.getCreatedAt()
                ),
                new CityDto(
                        city.getId(),
                        city.getName(),
                        city.getRegion(),
                        city.getCountry()
                ),
                new UserDto(
                        owner.getId(),
                        owner.getName(),
                        owner.getEmail(),
                        owner.getAvatarUrl(),
                        owner.getRole().name(),
                        owner.isBanned(),
                        new CityDto(
                                ownerCity.getId(),
                                ownerCity.getName(),
                                ownerCity.getRegion(),
                                ownerCity.getCountry()
                        )
                ),
                listing.getCondition(),
                imageUrls,
                listing.isOpen(),
                listing.isBlocked(),
                listing.getCreatedAt()
        );
    }

}
