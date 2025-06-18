package org.bookswap.listings.usecase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.bookswap.auth.entity.User;
import org.bookswap.auth.repository.UserRepo;
import org.bookswap.catalog.entity.AgeCategory;
import org.bookswap.catalog.entity.Book;
import org.bookswap.catalog.repository.BookRepo;
import org.bookswap.common.exception.BadRequestException;
import org.bookswap.common.exception.ForbiddenException;
import org.bookswap.common.exception.NotFoundException;
import org.bookswap.listings.dto.CreateListingDto;
import org.bookswap.listings.dto.ListingDto;
import org.bookswap.listings.dto.ListingFilterDto;
import org.bookswap.listings.dto.UpdateListingDto;
import org.bookswap.listings.entity.City;
import org.bookswap.listings.entity.Listing;
import org.bookswap.listings.entity.ListingImage;
import org.bookswap.listings.repository.CityRepo;
import org.bookswap.listings.repository.ListingImageRepo;
import org.bookswap.listings.repository.ListingRepo;
import org.bookswap.shared.image.ImageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.bookswap.auth.security.SecurityUtil.assertNotBanned;

@Service
@RequiredArgsConstructor
@Transactional
public class ListingUseCase {

    private final ListingRepo listingRepo;
    private final BookRepo bookRepo;
    private final UserRepo userRepo;
    private final CityRepo cityRepo;
    private final ListingImageRepo imageRepo;
    private final ImageService imageService;

    public ListingDto createListing(Long userId, CreateListingDto dto, List<MultipartFile> images) {
        User user = getActiveUser(userId);
        Book book = bookRepo.findById(dto.bookId())
                .orElseThrow(() -> new NotFoundException("Book not found"));
        City city = cityRepo.findById(dto.cityId())
                .orElseThrow(() -> new BadRequestException("City not found"));

        Listing listing = Listing.builder()
                .owner(user)
                .book(book)
                .condition(dto.condition())
                .city(city)
                .isOpen(true)
                .isBlocked(false)
                .createdAt(LocalDateTime.now())
                .build();

        listing = listingRepo.save(listing);

        if (images != null && images.size() > 3) {
            throw new BadRequestException("Max 3 images allowed");
        }

        if (images != null) {
            for (MultipartFile img : images) {
                String url = imageService.saveImage("listings", listing.getId(), img);
                imageRepo.save(ListingImage.builder()
                        .listing(listing)
                        .url(url)
                        .build());
            }
        }

        return toDto(listing);
    }

    public void updateListing(Long listingId, Long userId, String role, UpdateListingDto dto) {
        Listing listing = listingRepo.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));

        if (!Objects.equals(listing.getOwner().getId(), userId) &&
                !(role.equals("MODERATOR") || role.equals("ADMIN"))) {
            throw new ForbiddenException("Access denied");
        }

        if (dto.condition() != null) {
            listing.setCondition(dto.condition());
        }

        if (dto.cityId() != null) {
            City city = cityRepo.findById(dto.cityId())
                    .orElseThrow(() -> new BadRequestException("City not found"));
            listing.setCity(city);
        }

        listingRepo.save(listing);
    }

    public void deleteListing(Long listingId, Long userId, String role) {
        Listing listing = listingRepo.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));

        if (!Objects.equals(listing.getOwner().getId(), userId) &&
                !(role.equals("MODERATOR") || role.equals("ADMIN"))) {
            throw new ForbiddenException("Access denied");
        }

        imageRepo.findByListingId(listingId).forEach(img -> {
            imageService.deleteImageByUrl(img.getUrl());
            imageRepo.delete(img);
        });

        listingRepo.delete(listing);
    }

    public ListingDto getListing(Long id) {
        Listing listing = listingRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Listing not found"));

        return toDto(listing);
    }

    public Page<ListingDto> filterListings(ListingFilterDto filter, Pageable pageable) {
        String safeTitle  = (filter.title()  == null) ? "" : filter.title();
        String safeAuthor = (filter.author() == null) ? "" : filter.author();

        City city = null;
        if (filter.cityId() != null) {
            city = cityRepo.findById(filter.cityId())
                    .orElseThrow(() -> new BadRequestException("City not found"));
        }

        List<AgeCategory> ages   = (filter.ageCategories() == null || filter.ageCategories().isEmpty())
                ? null : filter.ageCategories();
        List<Long> genres        = (filter.genreIds()      == null || filter.genreIds().isEmpty())
                ? null : filter.genreIds();

        Page<Listing> page = listingRepo.searchListingsFull(
                safeTitle,
                safeAuthor,
                ages,
                genres,
                city,
                filter.condition(),
                pageable
        );

        return page.map(this::toDto);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private User getActiveUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        assertNotBanned(user);
        return user;
    }

    private ListingDto toDto(Listing listing) {
        List<String> urls = imageRepo.findByListingId(listing.getId()).stream()
                .map(ListingImage::getUrl)
                .toList();

        return new ListingDto(
                listing.getId(),
                listing.getBook().getId(),
                listing.getBook().getTitle(),
                listing.getBook().getAuthor(),
                listing.getCondition(),
                listing.getCity().getId(),
                listing.getCity().getName(),
                urls,
                listing.isOpen(),
                listing.isBlocked(),
                listing.getOwner().getId(),
                listing.getCreatedAt()
        );
    }
}
