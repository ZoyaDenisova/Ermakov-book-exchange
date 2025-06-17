package org.bookswap.listings.repository;

import org.bookswap.listings.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepo extends JpaRepository<City, Long> {

    // Поиск по точному названию города (без учета регистра)
    Optional<City> findByNameIgnoreCase(String name);

    // Поиск по названию и региону
    Optional<City> findByNameIgnoreCaseAndRegionIgnoreCase(String name, String region);
}
