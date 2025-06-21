package org.bookswap.listings.usecase;

import lombok.RequiredArgsConstructor;
import org.bookswap.auth.entity.User;
import org.bookswap.auth.repository.UserRepo;
import org.bookswap.auth.security.AuthContext;
import org.bookswap.auth.security.SecurityUtil;
import org.bookswap.catalog.entity.AgeCategory;
import org.bookswap.catalog.entity.Book;
import org.bookswap.catalog.repository.BookImageRepo;
import org.bookswap.catalog.repository.BookRepo;
import org.bookswap.common.exception.BadRequestException;
import org.bookswap.common.exception.ForbiddenException;
import org.bookswap.common.exception.NotFoundException;
import org.bookswap.listings.dto.*;
import org.bookswap.listings.entity.City;
import org.bookswap.listings.entity.Listing;
import org.bookswap.listings.entity.ListingImage;
import org.bookswap.listings.mapper.CityMapper;
import org.bookswap.listings.mapper.ListingMapper;
import org.bookswap.listings.repository.CityRepo;
import org.bookswap.listings.repository.ListingImageRepo;
import org.bookswap.listings.repository.ListingRepo;
import org.bookswap.shared.image.ImageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final BookImageRepo bookImageRepo;
    private final UserRepo userRepo;
    private final CityRepo cityRepo;
    private final ListingImageRepo imageRepo;
    private final ImageService imageService;
    private final ListingMapper listingMapper;
    private final CityMapper cityMapper;

    @Transactional
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

        return listingMapper.toDto(listing);
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

        return listingMapper.toDto(listing);
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
                filter.isBlocked(),
                pageable
        );

        return page.map(listingMapper::toDto);
    }


    @Transactional
    public void closeListing(Long id, AuthContext auth) {
        Listing listing = listingRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Listing not found"));

        SecurityUtil.assertIsSelfOrThrow(auth.getUserId(), listing.getOwner().getId());

        listing.setOpen(false);
    }

    @Transactional
    public void blockListing(Long id, AuthContext auth) {
        SecurityUtil.assertHasRole(auth.getRole(), org.bookswap.auth.entity.Role.ADMIN, org.bookswap.auth.entity.Role.MODERATOR);

        Listing listing = listingRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Listing not found"));

        listing.setBlocked(true);
    }

    @Transactional
    public void unblockListing(Long id, AuthContext auth) {
        SecurityUtil.assertHasRole(auth.getRole(), org.bookswap.auth.entity.Role.ADMIN, org.bookswap.auth.entity.Role.MODERATOR);

        Listing listing = listingRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Listing not found"));

        listing.setBlocked(false);
    }

    @Transactional(readOnly = true)
    public Page<ListingDto> getAllVisibleListingsByUser(Long userId, Pageable pageable) {
        return listingRepo.findAllVisibleByUserIdOrdered(userId, pageable)
                .map(listingMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<CityDto> searchCities(String query) {
        if (query == null || query.isBlank()) return List.of();

        Pageable pageable = PageRequest.of(0, 10); // лимит по умолчанию
        return cityRepo.searchCityByNameOrRegion(query, pageable).stream()
                .map(cityMapper::toDto)
                .toList();
    }

    private User getActiveUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        assertNotBanned(user);
        return user;
    }
}
