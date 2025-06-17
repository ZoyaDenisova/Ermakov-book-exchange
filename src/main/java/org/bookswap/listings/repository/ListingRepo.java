package org.bookswap.listings.repository;

import org.bookswap.auth.entity.User;
import org.bookswap.listings.entity.BookCondition;
import org.bookswap.listings.entity.City;
import org.bookswap.listings.entity.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ListingRepo extends JpaRepository<Listing, Long> {

    // Все открытые объявления (например, главная страница)
    List<Listing> findByIsOpenTrueAndIsBlockedFalse();

    // Открытые объявления по книге
    List<Listing> findByBookIdAndIsOpenTrueAndIsBlockedFalse(Long bookId);

    // Объявления конкретного пользователя (например, в "я могу предложить")
    List<Listing> findByOwnerIdAndIsOpenTrueAndIsBlockedFalse(Long userId);

    // Проверка: есть ли открытое объявление по конкретной книге от пользователя
    boolean existsByOwnerIdAndBookIdAndIsOpenTrueAndIsBlockedFalse(Long userId, Long bookId);

    // Объявления по городу, книге и состоянию
    @Query("""
        SELECT l FROM Listing l
        WHERE l.isOpen = true AND l.isBlocked = false
          AND (:bookId IS NULL OR l.book.id = :bookId)
          AND (:city IS NULL OR l.city = :city)
          AND (:condition IS NULL OR l.condition = :condition)
    """)
    Page<Listing> searchListings(@Param("bookId") Long bookId,
                                 @Param("city") City city,
                                 @Param("condition") BookCondition condition,
                                 Pageable pageable);

    // Заблокировать все объявления пользователя (например, при бане)
    @Modifying
    @Query("UPDATE Listing l SET l.isBlocked = true WHERE l.owner = :user")
    void blockAllListingsByUser(@Param("user") User user);

    // Закрыть объявление вручную (например, после успешного обмена)
    @Modifying
    @Query("UPDATE Listing l SET l.isOpen = false WHERE l.id = :id")
    void closeListing(@Param("id") Long listingId);

    // Удалить все объявления по книге (например, если книга удаляется)
    void deleteByBookId(Long bookId);
}
