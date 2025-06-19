package org.bookswap.catalog.usecase;

import lombok.RequiredArgsConstructor;
import org.bookswap.auth.entity.User;
import org.bookswap.auth.repository.UserRepo;
import org.bookswap.catalog.dto.BookDto;
import org.bookswap.catalog.dto.CreateBookDto;
import org.bookswap.catalog.dto.GenreDto;
import org.bookswap.catalog.dto.UpdateBookDto;
import org.bookswap.catalog.entity.*;
import org.bookswap.catalog.repository.BookImageRepo;
import org.bookswap.catalog.repository.BookRepo;
import org.bookswap.catalog.repository.GenreRepo;
import org.bookswap.catalog.repository.WantedBookRepo;
import org.bookswap.common.exception.BadRequestException;
import org.bookswap.common.exception.ForbiddenException;
import org.bookswap.common.exception.NotFoundException;
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
import java.util.Set;
import java.util.stream.Collectors;

import static org.bookswap.auth.security.SecurityUtil.assertNotBanned;

@Service
@RequiredArgsConstructor
@Transactional
public class CatalogUseCase {

    private final BookRepo bookRepo;
    private final GenreRepo genreRepo;
    private final BookImageRepo bookImageRepo;
    private final WantedBookRepo wantedBookRepo;
    private final UserRepo userRepo;
    private final ImageService imageService;

    public BookDto addBook(Long userId, String role, CreateBookDto dto, MultipartFile file) {
        User user = getActiveUser(userId);

        Set<Genre> genres = dto.genreIds().stream()
                .map(id -> genreRepo.findById(id)
                        .orElseThrow(() -> new BadRequestException("Genre not found: " + id)))
                .collect(Collectors.toSet());

        ModerationStatus status = ModerationStatus.APPROVED;

        Book book = Book.builder()
                .title(dto.title())
                .author(dto.author())
                .year(dto.year())
                .description(dto.description())
                .genres(genres)
                .ageCategory(dto.ageCategory())
                .createdBy(user)
                .moderationStatus(status)
                .createdAt(LocalDateTime.now())
                .build();

        book = bookRepo.save(book);

        if (file != null && !file.isEmpty()) {
            // Удаляем старые изображения
            bookImageRepo.findByBookId(book.getId()).forEach(img -> {
                imageService.deleteImageByUrl(img.getUrl());
                bookImageRepo.delete(img);
            });

            // Сохраняем новое
            String url = imageService.saveImage("books", book.getId(), file);
            bookImageRepo.save(BookImage.builder()
                    .book(book)
                    .url(url)
                    .build());
        }

        return toDto(book);
    }

    public void uploadImage(Long bookId, Long userId, String role, MultipartFile file) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        boolean isOwner = Objects.equals(book.getCreatedBy().getId(), userId);
        boolean isModerator = role.equals("MODERATOR") || role.equals("ADMIN");

        if (!isOwner && !isModerator) {
            throw new ForbiddenException("You are not allowed to upload image for this book");
        }

        // Удаляем старые изображения
        bookImageRepo.findByBookId(bookId).forEach(img -> {
            imageService.deleteImageByUrl(img.getUrl());
            bookImageRepo.delete(img);
        });

        // Сохраняем новое
        String url = imageService.saveImage("books", bookId, file);
        bookImageRepo.save(BookImage.builder()
                .book(book)
                .url(url)
                .build());
    }

    public BookDto getBook(Long id) {
        return bookRepo.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Book not found"));
    }

//    public Page<BookDto> searchApprovedBooks(String query, Pageable pageable) {
//        return bookRepo.globalBookSearch(query, pageable).map(this::toDto);
//    }

    public Page<BookDto> filterBooks(String title, String author, List<AgeCategory> ageCategories,
                                     List<Long> genreIds, Pageable pageable) {
        return bookRepo.searchBooks(title, author, ageCategories, genreIds, pageable)
                .map(this::toDto);
    }

    public void addWantedBook(Long userId, Long bookId) {
        User user = getActiveUser(userId);
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        if (wantedBookRepo.existsByUserIdAndBookId(userId, bookId)) {
            throw new BadRequestException("Already added to wanted list");
        }

        WantedBook wanted = WantedBook.builder()
                .user(user)
                .book(book)
                .createdAt(LocalDateTime.now())
                .build();

        wantedBookRepo.save(wanted);
    }

    public void removeWantedBook(Long userId, Long bookId) {
        getActiveUser(userId);
        wantedBookRepo.deleteByUserIdAndBookId(userId, bookId);
    }

    public Page<BookDto> getUserWantedBooks(Long userId, Pageable pageable) {
        return wantedBookRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(w -> toDto(w.getBook()));
    }

    @Transactional(readOnly = true)
    public List<String> autocompleteTitles(String prefix) {
        if (prefix == null || prefix.isBlank()) return List.of();
        Pageable pageable = PageRequest.of(0, 10);
        return bookRepo.autocompleteTitles(prefix, pageable);
    }

    @Transactional(readOnly = true)
    public List<String> autocompleteAuthors(String prefix) {
        if (prefix == null || prefix.isBlank()) return List.of();
        Pageable pageable = PageRequest.of(0, 10);
        return bookRepo.autocompleteAuthors(prefix, pageable);
    }


    public long getWantedCount(Long bookId) {
        return wantedBookRepo.countByBookId(bookId);
    }

    private User getActiveUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        assertNotBanned(user);
        return user;
    }

    public void deleteBook(Long bookId) {
        if (!bookRepo.existsById(bookId)) {
            throw new NotFoundException("Book not found");
        }
        bookRepo.deleteById(bookId);
    }

    public List<GenreDto> getGenres() {
        return genreRepo.findAll().stream()
                .map(g -> new GenreDto(g.getId(), g.getName()))
                .toList();
    }

    public void updateBook(Long bookId, Long userId, String role, UpdateBookDto dto) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        boolean isOwner = Objects.equals(book.getCreatedBy().getId(), userId);
        boolean isModerator = role.equals("MODERATOR") || role.equals("ADMIN");

        if (!isOwner && !isModerator) {
            throw new ForbiddenException("You are not allowed to edit this book");
        }

        if (isOwner) assertNotBanned(book.getCreatedBy());

        if (dto.title() != null) book.setTitle(dto.title());
        if (dto.author() != null) book.setAuthor(dto.author());
        if (dto.year() != null) book.setYear(dto.year());
        if (dto.description() != null) book.setDescription(dto.description());
        if (dto.ageCategory() != null) book.setAgeCategory(dto.ageCategory());
        if (dto.genreIds() != null) {
            Set<Genre> genres = dto.genreIds().stream()
                    .map(id -> genreRepo.findById(id)
                            .orElseThrow(() -> new BadRequestException("Genre not found: " + id)))
                    .collect(Collectors.toSet());
            book.setGenres(genres);
        }

        // Если редактирует обычный пользователь — книга уходит на повторную модерацию
        if (isOwner) {
            book.setModerationStatus(ModerationStatus.PENDING);
        }

        bookRepo.save(book);
    }

    private BookDto toDto(Book book) {
        String imageUrl = bookImageRepo.findByBookId(book.getId()).stream()
                .map(BookImage::getUrl)
                .findFirst()
                .orElse(null);

        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getYear(),
                book.getDescription(),
                book.getGenres().stream()
                        .map(Genre::getName)
                        .sorted()
                        .toList(),
                book.getAgeCategory(),
                imageUrl,
                book.getModerationStatus(),
                book.getCreatedBy() != null ? book.getCreatedBy().getId() : null,
                book.getCreatedAt()
        );
    }

    public void deleteImage(Long imageId, Long userId, String role) {
        BookImage image = bookImageRepo.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found"));

        Book book = image.getBook();
        boolean isOwner = Objects.equals(book.getCreatedBy().getId(), userId);
        boolean isModerator = role.equals("MODERATOR") || role.equals("ADMIN");

        if (!isOwner && !isModerator) {
            throw new ForbiddenException("You are not allowed to delete this image");
        }

        imageService.deleteImageByUrl(image.getUrl());
        bookImageRepo.delete(image);
    }
}