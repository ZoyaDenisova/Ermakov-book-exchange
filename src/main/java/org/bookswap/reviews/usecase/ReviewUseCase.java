package org.bookswap.reviews.usecase;

import lombok.RequiredArgsConstructor;
import org.bookswap.auth.entity.Role;
import org.bookswap.auth.entity.User;
import org.bookswap.auth.repository.UserRepo;
import org.bookswap.auth.security.SecurityUtil;
import org.bookswap.catalog.entity.ModerationStatus;
import org.bookswap.common.exception.BadRequestException;
import org.bookswap.common.exception.ConflictException;
import org.bookswap.common.exception.NotFoundException;
import org.bookswap.listings.entity.Listing;
import org.bookswap.listings.repository.ListingRepo;
import org.bookswap.reviews.dto.ComplaintDto;
import org.bookswap.reviews.dto.CreateComplaintDto;
import org.bookswap.reviews.dto.CreateReviewDto;
import org.bookswap.reviews.dto.ReviewDto;
import org.bookswap.reviews.entity.Complaint;
import org.bookswap.reviews.entity.ComplaintImage;
import org.bookswap.reviews.entity.Review;
import org.bookswap.reviews.entity.ReviewImage;
import org.bookswap.reviews.repository.ComplaintImageRepo;
import org.bookswap.reviews.repository.ComplaintRepo;
import org.bookswap.reviews.repository.ReviewImageRepo;
import org.bookswap.reviews.repository.ReviewRepo;
import org.bookswap.shared.image.ImageService;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewUseCase {

    private final ReviewRepo reviewRepo;
    private final ComplaintRepo complaintRepo;
    private final ReviewImageRepo reviewImageRepo;
    private final ComplaintImageRepo complaintImageRepo;
    private final ListingRepo listingRepo;
    private final UserRepo userRepo;
    private final ImageService imageService;

    public void createReview(Long fromUserId, CreateReviewDto dto, List<MultipartFile> images) {
        validateImages(images);

        if (dto.rating() < 1 || dto.rating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        User fromUser = getActiveUser(fromUserId);
        Listing listing = getListingOrThrow(dto.listingId());
        User toUser = listing.getOwner();

        if (fromUser.getId().equals(toUser.getId())) {
            throw new BadRequestException("You cannot leave a review for yourself");
        }

        if (reviewRepo.existsByFromUserIdAndListingId(fromUserId, listing.getId())) {
            throw new ConflictException("Review already exists");
        }

        Review review = Review.builder()
                .listing(listing)
                .fromUser(fromUser)
                .toUser(toUser)
                .rating(dto.rating())
                .comment(dto.comment())
                .moderationStatus(ModerationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Review saved = reviewRepo.save(review);

        saveImages(images, saved.getId(), "reviews", url ->
                reviewImageRepo.save(ReviewImage.builder()
                        .review(saved)
                        .url(url)
                        .build())
        );
    }

    public void createComplaint(Long fromUserId, CreateComplaintDto dto, List<MultipartFile> images) {
        validateImages(images);

        User fromUser = getActiveUser(fromUserId);
        Listing listing = getListingOrThrow(dto.listingId());
        User toUser = listing.getOwner();

        if (fromUser.getId().equals(toUser.getId())) {
            throw new BadRequestException("You cannot complain about yourself");
        }

        Complaint complaint = Complaint.builder()
                .listing(listing)
                .fromUser(fromUser)
                .toUser(toUser)
                .comment(dto.comment())
                .isReviewed(false)
                .createdAt(LocalDateTime.now())
                .build();

        Complaint saved = complaintRepo.save(complaint);

        saveImages(images, saved.getId(), "complaints", url ->
                complaintImageRepo.save(ComplaintImage.builder()
                        .complaint(saved)
                        .url(url)
                        .build())
        );
    }
    public void approveReview(Long id, String role) {
        SecurityUtil.assertHasRole(role, Role.MODERATOR, Role.ADMIN);
        Review review = getReviewOrThrow(id);
        review.setModerationStatus(ModerationStatus.APPROVED);
    }

    public void rejectReview(Long id, String role) {
        SecurityUtil.assertHasRole(role, Role.MODERATOR, Role.ADMIN);
        Review review = getReviewOrThrow(id);
        review.setModerationStatus(ModerationStatus.REJECTED);
    }

    public void markComplaintReviewed(Long id, String role) {
        SecurityUtil.assertHasRole(role, Role.MODERATOR, Role.ADMIN);
        Complaint complaint = getComplaintOrThrow(id);
        complaint.setReviewed(true);
    }

    public List<ReviewDto> getApprovedReviewsForUser(Long userId) {
        return reviewRepo.findByToUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged()).stream()
                .filter(r -> r.getModerationStatus() == ModerationStatus.APPROVED)
                .map(this::toDto)
                .toList();
    }

    public double getAverageRatingForUser(Long userId) {
        List<Review> reviews = reviewRepo.findByToUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged()).stream()
                .filter(r -> r.getModerationStatus() == ModerationStatus.APPROVED)
                .toList();

        if (reviews.isEmpty()) return 0.0;

        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }

    public List<ReviewDto> getReviewsByListing(Long listingId) {
        return reviewRepo.findByListingIdOrderByCreatedAtDesc(listingId, Pageable.unpaged())
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<ComplaintDto> getComplaintsByListing(Long listingId) {
        return complaintRepo.findByListingIdOrderByCreatedAtDesc(listingId, Pageable.unpaged())
                .stream()
                .map(this::toDto)
                .toList();
    }

    private void validateImages(List<MultipartFile> images) {
        if (images != null && images.size() > 3) {
            throw new BadRequestException("Max 3 images allowed");
        }
    }

    private void saveImages(List<MultipartFile> images, Long entityId, String folder, Consumer<String> persistFunc) {
        if (images == null) return;
        for (MultipartFile img : images) {
            String url = imageService.saveImage(folder, entityId, img);
            persistFunc.accept(url);
        }
    }

    private User getActiveUser(Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        SecurityUtil.assertNotBanned(user);
        return user;
    }

    private Listing getListingOrThrow(Long id) {
        return listingRepo.findById(id).orElseThrow(() -> new NotFoundException("Listing not found"));
    }

    private Review getReviewOrThrow(Long id) {
        return reviewRepo.findById(id).orElseThrow(() -> new NotFoundException("Review not found"));
    }

    private Complaint getComplaintOrThrow(Long id) {
        return complaintRepo.findById(id).orElseThrow(() -> new NotFoundException("Complaint not found"));
    }

    private ReviewDto toDto(Review r) {
        List<String> urls = reviewImageRepo.findByReviewId(r.getId()).stream().map(ReviewImage::getUrl).toList();
        return new ReviewDto(r.getId(), r.getListing().getId(), r.getFromUser().getId(), r.getToUser().getId(),
                r.getRating(), r.getComment(), r.getModerationStatus(), urls, r.getCreatedAt());
    }

    private ComplaintDto toDto(Complaint c) {
        List<String> urls = complaintImageRepo.findByComplaintId(c.getId()).stream().map(ComplaintImage::getUrl).toList();
        return new ComplaintDto(c.getId(), c.getListing().getId(), c.getFromUser().getId(), c.getToUser().getId(),
                c.getComment(), c.isReviewed(), urls, c.getCreatedAt());
    }
}
