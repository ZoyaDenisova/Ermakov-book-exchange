package org.bookswap.catalog.mapper;

import lombok.RequiredArgsConstructor;
import org.bookswap.catalog.dto.BookDto;
import org.bookswap.catalog.entity.Book;
import org.bookswap.catalog.entity.BookImage;
import org.bookswap.catalog.entity.Genre;
import org.bookswap.catalog.repository.BookImageRepo;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookMapper {

    private final BookImageRepo bookImageRepo;

    public BookDto toDto(Book book) {
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
}
