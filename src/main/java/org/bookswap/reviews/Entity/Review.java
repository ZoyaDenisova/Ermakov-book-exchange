package org.bookswap.reviews.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.bookswap.auth.entity.User;
import org.bookswap.listings.entity.Listing;
import org.bookswap.catalog.entity.ModerationStatus;

import java.time.LocalDateTime;
@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id")
    private User toUser;

    private int rating;
    private String comment;

    @Enumerated(EnumType.STRING)
    private ModerationStatus moderationStatus;

    private LocalDateTime createdAt;
}

