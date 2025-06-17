package org.bookswap.auth.repository;

import org.bookswap.auth.entity.User;
import org.bookswap.listings.entity.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // Универсальный поиск по имени, городу и статусу блокировки
    @Query("SELECT u FROM User u WHERE " +
            "(:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:city IS NULL OR u.city = :city) AND " +
            "(:banned IS NULL OR u.isBanned = :banned)")
    Page<User> searchUsers(@Param("name") String name,
                           @Param("city") City city,
                           @Param("banned") Boolean banned,
                           Pageable pageable);
    @Modifying
    @Query("UPDATE User u SET u.isBanned = true WHERE u.id = :id")
    void banUser(@Param("id") Long id);

    @Modifying
    @Query("UPDATE User u SET u.isBanned = false WHERE u.id = :id")
    void unbanUser(@Param("id") Long id);

}

