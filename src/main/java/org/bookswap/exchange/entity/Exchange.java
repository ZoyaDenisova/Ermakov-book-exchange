package org.bookswap.exchange.entity;

import jakarta.persistence.*;
import lombok.*;
import org.bookswap.auth.entity.User;
import org.bookswap.listings.entity.Listing;

import java.time.LocalDateTime;

@Entity
@Table(name = "exchanges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exchange {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_listing_id")
    private Listing offered;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_listing_id")
    private Listing selected;

    @Enumerated(EnumType.STRING)
    private ExchangeStatus status;

    private boolean senderConfirmedCompletion;
    private boolean receiverConfirmedCompletion;

    private LocalDateTime completedAt; //Когда оба подтвердили обмен или кто-то отклонил
    private LocalDateTime createdAt;
}
