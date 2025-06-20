package org.bookswap.messaging.repository;

import org.bookswap.messaging.entity.Dialog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DialogRepo extends JpaRepository<Dialog, Long> {

    // Получить все диалоги пользователя (и владельца, и участника)
    @Query("""
        SELECT d FROM Dialog d
        WHERE d.owner.id = :userId OR d.otherParticipant.id = :userId
        ORDER BY (
            SELECT MAX(m.createdAt) FROM Message m WHERE m.dialog = d
        ) DESC
    """)
    List<Dialog> findUserDialogsOrderedByLastMessage(@Param("userId") Long userId);

    @Query("""
        select d
        from Dialog d
        where d.listing.id = :listingId
          and (d.owner.id = :userId or d.otherParticipant.id = :userId)
        """)
    Optional<Dialog> findByListingIdAndUserId(
            @Param("listingId") Long listingId,
            @Param("userId")    Long userId
    );
}
