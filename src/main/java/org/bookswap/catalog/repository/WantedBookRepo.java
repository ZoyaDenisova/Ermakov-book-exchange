package org.bookswap.catalog.repository;

import org.bookswap.catalog.entity.WantedBook;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

public interface WantedBookRepo extends JpaRepository<WantedBook, Long> {

    // Проверить, хочет ли конкретный пользователь эту книгу
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    // Найти все "хочу" книги пользователя
    Page<WantedBook> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Удалить "хотелку"
    void deleteByUserIdAndBookId(Long userId, Long bookId);

    // Для страницы книги — узнать, сколько людей её хотят
    long countByBookId(Long bookId);
}