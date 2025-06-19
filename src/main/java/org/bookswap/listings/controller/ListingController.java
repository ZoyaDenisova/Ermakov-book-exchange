package org.bookswap.listings.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.bookswap.auth.security.AuthContext;
import org.bookswap.auth.security.TokenManager;
import org.bookswap.listings.dto.*;
import org.bookswap.listings.usecase.ListingUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Operation(summary = "Получить все незаблокированные объявления пользователя (сначала открытые)")
    @GetMapping("/user/{userId}/all")
    public ResponseEntity<Page<ListingDto>> getAllUserListings(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(listingUseCase.getAllVisibleListingsByUser(userId, pageable));
    }

    @Operation(summary = "Закрыть объявление (владелец)")
    @PatchMapping("/{id}/close")
    public ResponseEntity<Void> closeListing(@PathVariable Long id, HttpServletRequest request) {
        AuthContext auth = new AuthContext(request, tokenManager);
        listingUseCase.closeListing(id, auth);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Заблокировать объявление (модератор/админ)")
    @PatchMapping("/{id}/block")
    public ResponseEntity<Void> blockListing(@PathVariable Long id, HttpServletRequest request) {
        AuthContext auth = new AuthContext(request, tokenManager);
        listingUseCase.blockListing(id, auth);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Разблокировать объявление (модератор/админ)")
    @PatchMapping("/{id}/unblock")
    public ResponseEntity<Void> unblockListing(@PathVariable Long id, HttpServletRequest request) {
        AuthContext auth = new AuthContext(request, tokenManager);
        listingUseCase.unblockListing(id, auth);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Поиск городов по названию или региону (префиксный)")
    @GetMapping("/cities/search")
    public ResponseEntity<List<CityDto>> searchCities(@RequestParam String query) {
        return ResponseEntity.ok(listingUseCase.searchCities(query));
    }
}
