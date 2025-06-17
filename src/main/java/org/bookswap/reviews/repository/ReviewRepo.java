package org.bookswap.reviews.repository;

import org.bookswap.reviews.entity.Review;
import org.bookswap.catalog.entity.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepo extends JpaRepository<Review, Long> {

    // Все отзывы, направленные к пользователю
    Page<Review> findByToUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Все отзывы, оставленные конкретным пользователем
    Page<Review> findByFromUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Отзывы по объявлению
    Page<Review> findByListingIdOrderByCreatedAtDesc(Long listingId, Pageable pageable);

    // Отзывы в определённом статусе — для модерации
    Page<Review> findByModerationStatusOrderByCreatedAtDesc(ModerationStatus status, Pageable pageable);

    // Проверка — оставлял ли пользователь отзыв по объявлению
    boolean existsByFromUserIdAndListingId(Long fromUserId, Long listingId);

    // Удалить все отзывы по объявлению
    void deleteByListingId(Long listingId);
}
