package org.bookswap.messaging.repository;

import org.bookswap.messaging.entity.Dialog;
import org.bookswap.auth.entity.User;
import org.bookswap.listings.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DialogRepo extends JpaRepository<Dialog, Long> {

    // Найти диалог между двумя пользователями по объявлению (уникальный)
    Optional<Dialog> findByUser1AndUser2AndListing(User user1, User user2, Listing listing);

    // Получить все диалоги пользователя
    @Query("""
    SELECT d FROM Dialog d
    WHERE d.user1.id = :userId OR d.user2.id = :userId
    ORDER BY (
        SELECT MAX(m.createdAt) FROM Message m WHERE m.dialog = d
    ) DESC
""")
    List<Dialog> findUserDialogsOrderedByLastMessage(@Param("userId") Long userId);


    // Все диалоги по объявлению (например, для владельца)
    @Query("""
    SELECT d FROM Dialog d
    WHERE d.listing.id = :listingId
    ORDER BY (
        SELECT MAX(m.createdAt) FROM Message m WHERE m.dialog = d
    ) DESC
""")
    List<Dialog> findDialogsByListingOrderedByLastMessage(@Param("listingId") Long listingId);

    @Query("""
SELECT d FROM Dialog d
WHERE ((d.user1.id = :user1Id AND d.user2.id = :user2Id) OR (d.user1.id = :user2Id AND d.user2.id = :user1Id))
  AND d.listing.id = :listingId
""")
    Optional<Dialog> findBetweenUsersForListing(@Param("user1Id") Long user1Id,
                                                @Param("user2Id") Long user2Id,
                                                @Param("listingId") Long listingId);

}

