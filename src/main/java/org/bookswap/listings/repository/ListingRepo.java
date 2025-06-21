package org.bookswap.listings.repository;

import org.bookswap.auth.entity.User;
import org.bookswap.catalog.entity.AgeCategory;
import org.bookswap.listings.entity.BookCondition;
import org.bookswap.listings.entity.City;
import org.bookswap.listings.entity.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ListingRepo extends JpaRepository<Listing, Long> {

    @Query("""
    SELECT DISTINCT l FROM Listing l
    JOIN l.book b
    LEFT JOIN b.genres g
    WHERE l.isOpen = true AND l.isBlocked = false
      AND (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
      AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')))
      AND (:ageCategories IS NULL OR b.ageCategory IN :ageCategories)
      AND (:genreIds IS NULL OR g.id IN :genreIds)
    ORDER BY l.createdAt DESC
""")
    Page<Listing> searchListingsByBookData(@Param("title") String title,
                                           @Param("author") String author,
                                           @Param("ageCategories") List<AgeCategory> ageCategories,
                                           @Param("genreIds") List<Long> genreIds,
                                           Pageable pageable);


    @Query("""
    SELECT l FROM Listing l
    WHERE l.book.id = :bookId
      AND l.isOpen = true
      AND l.isBlocked = false
      AND (:cityId IS NULL OR l.city.id = :cityId)
    ORDER BY l.createdAt DESC
""")
    List<Listing> findOpenListingsByBookAndCity(@Param("bookId") Long bookId,
                                                @Param("cityId") Long cityId,
                                                Pageable pageable);


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
    SELECT DISTINCT l FROM Listing l
    JOIN l.book b
    LEFT JOIN b.genres g
    WHERE l.isOpen = true
      AND (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
      AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')))
      AND (:ageCategories IS NULL OR b.ageCategory IN :ageCategories)
      AND (:genreIds IS NULL OR g.id IN :genreIds)
      AND (:city IS NULL OR l.city = :city)
      AND (:condition IS NULL OR l.condition = :condition)
      AND (:isBlocked IS NULL OR l.isBlocked = :isBlocked)
    ORDER BY l.createdAt DESC
""")
    Page<Listing> searchListingsFull(@Param("title") String title,
                                     @Param("author") String author,
                                     @Param("ageCategories") List<AgeCategory> ageCategories,
                                     @Param("genreIds") List<Long> genreIds,
                                     @Param("city") City city,
                                     @Param("condition") BookCondition condition,
                                     @Param("isBlocked") Boolean isBlocked,
                                     Pageable pageable);

    // Заблокировать все объявления пользователя (например, при бане)
    @Modifying
    @Query("UPDATE Listing l SET l.isBlocked = true WHERE l.owner = :user")
    void blockAllListingsByUser(@Param("user") User user);

    // Закрыть объявление вручную (например, после успешного обмена)
    @Modifying
    @Query("UPDATE Listing l SET l.isOpen = false WHERE l.id = :id")
    void closeListing(@Param("id") Long listingId);

    @Query("""
    SELECT l FROM Listing l
    WHERE l.owner.id = :userId AND l.isBlocked = false
    ORDER BY l.isOpen DESC, l.createdAt DESC
""")
    Page<Listing> findAllVisibleByUserIdOrdered(@Param("userId") Long userId, Pageable pageable);

    //Автокомплит по названию
    @Query("""
    SELECT DISTINCT b.title FROM Listing l
    JOIN l.book b
    WHERE l.isOpen = true AND l.isBlocked = false
      AND LOWER(b.title) LIKE LOWER(CONCAT(:prefix, '%'))
    ORDER BY b.title
""")
    List<String> autocompleteTitlesFromListings(@Param("prefix") String prefix, Pageable pageable);

    //Автокомплит по автору
    @Query("""
    SELECT DISTINCT b.author FROM Listing l
    JOIN l.book b
    WHERE l.isOpen = true AND l.isBlocked = false
      AND LOWER(b.author) LIKE LOWER(CONCAT(:prefix, '%'))
    ORDER BY b.author
""")
    List<String> autocompleteAuthorsFromListings(@Param("prefix") String prefix, Pageable pageable);

}

