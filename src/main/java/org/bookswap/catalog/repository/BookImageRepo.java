package org.bookswap.catalog.repository;

import org.bookswap.catalog.entity.BookImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookImageRepo extends JpaRepository<BookImage, Long> {
    List<BookImage> findByBookId(Long bookId);
}
