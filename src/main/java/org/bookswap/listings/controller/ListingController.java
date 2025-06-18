package org.bookswap.listings.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.bookswap.auth.security.AuthContext;
import org.bookswap.auth.security.TokenManager;
import org.bookswap.listings.dto.CreateListingDto;
import org.bookswap.listings.dto.ListingDto;
import org.bookswap.listings.dto.ListingFilterDto;
import org.bookswap.listings.dto.UpdateListingDto;
import org.bookswap.listings.usecase.ListingUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingUseCase listingUseCase;
    private final TokenManager tokenManager;

    @Operation(summary = "Создать новое объявление")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ListingDto> createListing(
            @RequestPart("data") CreateListingDto dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            HttpServletRequest request
    ) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        return ResponseEntity.ok(listingUseCase.createListing(ctx.getUserId(), dto, images));
    }

    @Operation(summary = "Обновить объявление")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateListing(
            @PathVariable Long id,
            @RequestBody UpdateListingDto dto,
            HttpServletRequest request
    ) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        listingUseCase.updateListing(id, ctx.getUserId(), ctx.getRole(), dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить объявление")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        listingUseCase.deleteListing(id, ctx.getUserId(), ctx.getRole());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить объявление по ID")
    @GetMapping("/{id}")
    public ResponseEntity<ListingDto> getListing(@PathVariable Long id) {
        return ResponseEntity.ok(listingUseCase.getListing(id));
    }

    @Operation(summary = "Фильтрация объявлений")
    @PostMapping("/filter")
    public ResponseEntity<Page<ListingDto>> filterListings(
            @RequestBody ListingFilterDto filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                listingUseCase.filterListings(
                        filter,
                        PageRequest.of(page, size)
                )
        );
    }

}
