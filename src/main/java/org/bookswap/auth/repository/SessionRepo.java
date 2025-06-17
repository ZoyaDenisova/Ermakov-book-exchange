package org.bookswap.auth.repository;
import org.bookswap.auth.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface SessionRepo extends JpaRepository<Session, Long> {
    Optional<Session> findByRefreshToken(String refreshToken);
    List<Session> findByUserId(Long userId);

    // logout одного устройства
    void deleteByRefreshToken(String refreshToken);

    // logout со всех устройств
    void deleteByUserId(Long userId);
}

