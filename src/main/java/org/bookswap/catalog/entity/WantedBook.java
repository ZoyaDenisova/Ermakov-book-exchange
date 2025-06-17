package org.bookswap.catalog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bookswap.auth.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "wanted_books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WantedBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    private LocalDateTime createdAt;
}

