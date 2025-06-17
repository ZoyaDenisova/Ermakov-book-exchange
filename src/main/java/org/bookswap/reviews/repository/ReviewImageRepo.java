package org.bookswap.reviews.repository;

import org.bookswap.reviews.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepo extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findByReviewId(Long reviewId);
    void deleteByReviewId(Long reviewId);
}
