package org.bookswap.reviews.repository;

import org.bookswap.reviews.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepo extends JpaRepository<Complaint, Long> {
    Page<Complaint> findAllByIsReviewedOrderByCreatedAtDesc(boolean isReviewed, Pageable pageable);

    Page<Complaint> findByListingIdOrderByCreatedAtDesc(Long listingId, Pageable pageable);
}
