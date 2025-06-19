package org.bookswap.reviews.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.bookswap.auth.security.AuthContext;
import org.bookswap.auth.security.TokenManager;
import org.bookswap.reviews.dto.*;
import org.bookswap.reviews.usecase.ReviewUseCase;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewUseCase reviewUseCase;
    private final TokenManager tokenManager;

    @Operation(summary = "Оставить отзыв на пользователя")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createReview(
            @RequestPart("data") CreateReviewDto dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            HttpServletRequest request
    ) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        reviewUseCase.createReview(ctx.getUserId(), dto, images);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Оставить жалобу на пользователя")
    @PostMapping(value = "/complaint", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createComplaint(
            @RequestPart("data") CreateComplaintDto dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            HttpServletRequest request
    ) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        reviewUseCase.createComplaint(ctx.getUserId(), dto, images);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Одобрить отзыв (модератор/админ)")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveReview(@PathVariable Long id, HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        reviewUseCase.approveReview(id, ctx.getRole());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Отклонить отзыв (модератор/админ)")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> rejectReview(@PathVariable Long id, HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        reviewUseCase.rejectReview(id, ctx.getRole());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Пометить жалобу как рассмотренную (модератор/админ)")
    @PatchMapping("/complaint/{id}/reviewed")
    public ResponseEntity<Void> markComplaintReviewed(@PathVariable Long id, HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        reviewUseCase.markComplaintReviewed(id, ctx.getRole());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить список одобренных отзывов на пользователя")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDto>> getApprovedReviews(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewUseCase.getApprovedReviewsForUser(userId));
    }

    @Operation(summary = "Получить средний рейтинг пользователя")
    @GetMapping("/user/{userId}/rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewUseCase.getAverageRatingForUser(userId));
    }

    @Operation(summary = "Получить все отзывы по объявлению")
    @GetMapping("/listing/{listingId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByListing(@PathVariable Long listingId) {
        return ResponseEntity.ok(reviewUseCase.getReviewsByListing(listingId));
    }

    @Operation(summary = "Получить все жалобы по объявлению")
    @GetMapping("/listing/{listingId}/complaints")
    public ResponseEntity<List<ComplaintDto>> getComplaintsByListing(@PathVariable Long listingId) {
        return ResponseEntity.ok(reviewUseCase.getComplaintsByListing(listingId));
    }
}