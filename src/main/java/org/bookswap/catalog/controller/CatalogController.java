package org.bookswap.catalog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.bookswap.auth.entity.Role;
import org.bookswap.auth.security.AuthContext;
import org.bookswap.auth.security.SecurityUtil;
import org.bookswap.auth.security.TokenManager;
import org.bookswap.catalog.dto.*;
import org.bookswap.catalog.entity.AgeCategory;
import org.bookswap.catalog.usecase.CatalogUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogUseCase catalogUseCase;
    private final TokenManager tokenManager;

    @Operation(summary = "Создать новую книгу")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookDto> addBook(
            @RequestPart("data") CreateBookDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        return ResponseEntity.ok(catalogUseCase.addBook(ctx.getUserId(), ctx.getRole(), dto, file));
    }

    @Operation(summary = "Получить книгу по ID")
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBook(@PathVariable Long id) {
        return ResponseEntity.ok(catalogUseCase.getBook(id));
    }

    @Operation(summary = "Глобальный поиск по названию или автору")
    @GetMapping("/search")
    public ResponseEntity<Page<BookDto>> search(@RequestParam String query,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(catalogUseCase.searchApprovedBooks(query, PageRequest.of(page, size)));
    }

    @Operation(summary = "Фильтрация книг по параметрам")
    @GetMapping("/filter")
    public ResponseEntity<Page<BookDto>> filter(@RequestParam(required = false) String title,
                                                @RequestParam(required = false) String author,
                                                @RequestParam(required = false) List<AgeCategory> ageCategories,
                                                @RequestParam(required = false) List<Long> genreIds,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                catalogUseCase.filterBooks(title, author, ageCategories, genreIds, PageRequest.of(page, size)));
    }

    @Operation(summary = "Получить список книг, созданных текущим пользователем")
    @GetMapping("/user")
    public ResponseEntity<List<BookDto>> getUserBooks(HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        return ResponseEntity.ok(catalogUseCase.getUserBooks(ctx.getUserId()));
    }


    @Operation(summary = "Обновить книгу (частично)")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateBook(@PathVariable Long id,
                                           @RequestBody UpdateBookDto dto,
                                           HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        catalogUseCase.updateBook(id, ctx.getUserId(), ctx.getRole(), dto);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Удалить книгу по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        catalogUseCase.deleteBook(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Добавить книгу в список \"хочу\"")
    @PostMapping("/wanted/{bookId}")
    public ResponseEntity<Void> addWanted(@PathVariable Long bookId, HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        catalogUseCase.addWantedBook(ctx.getUserId(), bookId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить книгу из списка \"хочу\"")
    @DeleteMapping("/wanted/{bookId}")
    public ResponseEntity<Void> removeWanted(@PathVariable Long bookId, HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        catalogUseCase.removeWantedBook(ctx.getUserId(), bookId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить список книг из \"хочу\" текущего пользователя")
    @GetMapping("/wanted")
    public ResponseEntity<List<BookDto>> getMyWanted(HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        return ResponseEntity.ok(catalogUseCase.getUserWantedBooks(ctx.getUserId()));
    }

    @Operation(summary = "Получить количество пользователей, добавивших книгу в \"хочу\"")
    @GetMapping("/wanted/count/{bookId}")
    public ResponseEntity<Long> getWantedCount(@PathVariable Long bookId) {
        return ResponseEntity.ok(catalogUseCase.getWantedCount(bookId));
    }

    @Operation(summary = "Получить список всех жанров")
    @GetMapping("/genres")
    public ResponseEntity<List<GenreDto>> getGenres() {
        return ResponseEntity.ok(catalogUseCase.getGenres());
    }

    @Operation(summary = "Автокомплит авторов по префиксу")
    @GetMapping("/autocomplete/authors")
    public ResponseEntity<List<String>> autocompleteAuthors(@RequestParam String prefix,
                                                            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(catalogUseCase.autocompleteAuthors(prefix, PageRequest.of(0, size)));
    }

    @Operation(summary = "Автокомплит названий книг по префиксу")
    @GetMapping("/autocomplete/titles")
    public ResponseEntity<List<String>> autocompleteTitles(@RequestParam String prefix,
                                                           @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(catalogUseCase.autocompleteTitles(prefix, PageRequest.of(0, size)));
    }

    @Operation(summary = "Получить книги на модерацию")
    @GetMapping("/moderation")
    public ResponseEntity<List<BookDto>> getBooksForModeration(HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        SecurityUtil.assertHasRole(ctx.getRole(), Role.MODERATOR, Role.ADMIN);
        return ResponseEntity.ok(catalogUseCase.getBooksForModeration());
    }

    @Operation(summary = "Подтвердить книгу (модератор)")
    @PostMapping("/approve/{bookId}")
    public ResponseEntity<Void> approveBook(@PathVariable Long bookId, HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        SecurityUtil.assertHasRole(ctx.getRole(), Role.MODERATOR, Role.ADMIN);
        catalogUseCase.approveBook(bookId);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Отклонить книгу (модератор)")
    @PostMapping("/reject/{bookId}")
    public ResponseEntity<Void> rejectBook(@PathVariable Long bookId, HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        SecurityUtil.assertHasRole(ctx.getRole(), Role.MODERATOR, Role.ADMIN);
        catalogUseCase.rejectBook(bookId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Установить или заменить изображение книги (аватар)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Изображение загружено"),
            @ApiResponse(responseCode = "403", description = "Нет доступа"),
            @ApiResponse(responseCode = "404", description = "Книга не найдена")
    })
    @PutMapping(value = "/{bookId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadImage(
            @PathVariable Long bookId,
            @Parameter(description = "Файл изображения", required = true)
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        catalogUseCase.uploadImage(bookId, ctx.getUserId(), ctx.getRole(), file);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId, HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        catalogUseCase.deleteImage(imageId, ctx.getUserId(), ctx.getRole());
        return ResponseEntity.ok().build();
    }
}

