package org.bookswap.reviews.repository;

import org.bookswap.catalog.entity.ModerationStatus;
import org.bookswap.reviews.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepo extends JpaRepository<Review, Long> {

    // Все отзывы, направленные к пользователю
    Page<Review> findByToUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Отзывы по объявлению
    Page<Review> findByListingIdOrderByCreatedAtDesc(Long listingId, Pageable pageable);

    // Проверка — оставлял ли пользователь отзыв по объявлению
    boolean existsByFromUserIdAndListingId(Long fromUserId, Long listingId);

    Page<Review> findByToUserIdAndModerationStatusOrderByCreatedAtDesc(Long userId, ModerationStatus status, Pageable pageable);
}
