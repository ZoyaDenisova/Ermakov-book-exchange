package org.bookswap.messaging.entity;

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
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "other_participant_id")
    private User otherParticipant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private Listing listing;
}

