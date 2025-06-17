package org.bookswap.listings.repository;

import org.bookswap.listings.entity.City;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CityRepo extends JpaRepository<City, Long> {

    // Поиск по точному названию города (без учета регистра)
    Optional<City> findByNameIgnoreCase(String name);

    // Поиск по названию и региону
    Optional<City> findByNameIgnoreCaseAndRegionIgnoreCase(String name, String region);

    @Query("SELECT c FROM City c WHERE LOWER(c.name) LIKE LOWER(CONCAT(:prefix, '%'))")
    List<City> autocompleteCity(@Param("prefix") String prefix, Pageable pageable);
}
