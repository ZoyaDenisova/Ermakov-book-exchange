package org.bookswap.catalog.repository;

import org.bookswap.catalog.entity.WantedBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WantedBookRepo extends JpaRepository<WantedBook, Long> {

    // Проверить, хочет ли конкретный пользователь эту книгу
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    // Найти все "хочу" книги пользователя
    List<WantedBook> findByUserId(Long userId);

    // Удалить "хотелку"
    void deleteByUserIdAndBookId(Long userId, Long bookId);

    // Для страницы книги — узнать, сколько людей её хотят
    long countByBookId(Long bookId);
}