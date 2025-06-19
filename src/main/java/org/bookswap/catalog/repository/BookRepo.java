package org.bookswap.catalog.repository;

import org.bookswap.catalog.entity.AgeCategory;
import org.bookswap.catalog.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepo extends JpaRepository<Book, Long> {

    @Query("""
    SELECT DISTINCT b.title FROM Book b
    WHERE LOWER(b.title) LIKE LOWER(CONCAT(:prefix, '%'))
    ORDER BY b.title ASC
""")
    List<String> autocompleteTitles(@Param("prefix") String prefix, Pageable pageable);

    @Query("""
    SELECT DISTINCT b.author FROM Book b
    WHERE LOWER(b.author) LIKE LOWER(CONCAT(:prefix, '%'))
    ORDER BY b.author ASC
""")
    List<String> autocompleteAuthors(@Param("prefix") String prefix, Pageable pageable);

    // Поиск по названию/автору/жанру/возрасту (частичное совпадение, без учёта регистра)
    @Query("""
    SELECT DISTINCT b FROM Book b
    LEFT JOIN b.genres g
    WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
      AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')))
      AND (:ageCategories IS NULL OR b.ageCategory IN :ageCategories)
      AND (:genreIds IS NULL OR g.id IN :genreIds)
""")
    Page<Book> searchBooks(@Param("title") String title,
                           @Param("author") String author,
                           @Param("ageCategories") List<AgeCategory> ageCategories,
                           @Param("genreIds") List<Long> genreIds,
                           Pageable pageable);
}

