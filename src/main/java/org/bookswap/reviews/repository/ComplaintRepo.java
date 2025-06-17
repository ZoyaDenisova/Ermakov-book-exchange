package org.bookswap.reviews.repository;

import org.bookswap.reviews.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepo extends JpaRepository<Complaint, Long> {

    // Жалобы от пользователя (для личного кабинета)
    Page<Complaint> findByFromUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Жалобы на пользователя
    Page<Complaint> findByToUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Жалобы по объявлению
    Page<Complaint> findByListingIdOrderByCreatedAtDesc(Long listingId, Pageable pageable);

    // Непросмотренные (для модерации)
    Page<Complaint> findByIsReviewedFalseOrderByCreatedAtDesc(Pageable pageable);
    Page<Complaint> findByIsReviewedTrueOrderByCreatedAtDesc(Pageable pageable);

    // Удалить все жалобы по объявлению
    void deleteByListingId(Long listingId);
}
