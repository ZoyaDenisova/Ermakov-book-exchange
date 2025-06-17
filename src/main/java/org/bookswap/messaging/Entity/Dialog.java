package org.bookswap.messaging.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.bookswap.auth.entity.User;
import org.bookswap.listings.entity.Listing;

@Entity
@Table(name = "dialogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dialog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id")
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id")
    private User user2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private Listing listing;
}

