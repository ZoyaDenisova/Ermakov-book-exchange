package org.bookswap.reviews.mapper;

import lombok.RequiredArgsConstructor;
import org.bookswap.auth.mapper.UserMapper;
import org.bookswap.listings.mapper.ListingMapper;
import org.bookswap.reviews.dto.ReviewDto;
import org.bookswap.reviews.entity.Review;
import org.bookswap.reviews.entity.ReviewImage;
import org.bookswap.reviews.repository.ReviewImageRepo;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapper {

    private final ListingMapper listingMapper;
    private final UserMapper userMapper;
    private final ReviewImageRepo reviewImageRepo;

    public ReviewDto toDto(Review review) {
        return new ReviewDto(
                review.getId(),
                listingMapper.toDto(review.getListing()),
                userMapper.toDto(review.getFromUser()),
                userMapper.toDto(review.getToUser()),
                review.getRating(),
                review.getComment(),
                review.getModerationStatus(),
                reviewImageRepo.findByReviewId(review.getId()).stream()
                        .map(ReviewImage::getUrl)
                        .toList(),
                review.getCreatedAt()
        );
    }
}
