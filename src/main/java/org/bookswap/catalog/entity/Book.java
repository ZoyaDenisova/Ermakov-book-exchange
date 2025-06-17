package org.bookswap.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.bookswap.auth.entity.User;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;
    private Integer year;
    @Column(length = 2048)
    private String description;

    @ManyToMany
    @JoinTable(
            name = "book_genres",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres;

    @Enumerated(EnumType.STRING)
    private AgeCategory ageCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Enumerated(EnumType.STRING)
    private ModerationStatus moderationStatus;

    private LocalDateTime createdAt;
}

