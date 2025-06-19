package org.bookswap.listings.repository;

import org.bookswap.listings.entity.City;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CityRepo extends JpaRepository<City, Long> {

    @Query("""
    SELECT c FROM City c
    WHERE LOWER(c.name) LIKE LOWER(CONCAT(:query, '%'))
       OR LOWER(c.region) LIKE LOWER(CONCAT(:query, '%'))
    ORDER BY c.name ASC
""")
    List<City> searchCityByNameOrRegion(@Param("query") String query, Pageable pageable);
}
