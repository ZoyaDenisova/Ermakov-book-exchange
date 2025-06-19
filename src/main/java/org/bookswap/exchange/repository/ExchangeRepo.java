package org.bookswap.exchange.repository;

import org.bookswap.exchange.entity.Exchange;
import org.bookswap.exchange.entity.ExchangeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ExchangeRepo extends JpaRepository<Exchange, Long> {
    // Проверка, есть ли уже запрос обмена между двумя листингами (чтобы не дублировать)
    Optional<Exchange> findByOfferedIdAndSelectedId(Long offeredId, Long selectedId);

    // Проверить, участвует ли листинг в каком-то активном обмене
    @Query("""
    SELECT COUNT(e) > 0 FROM Exchange e
    WHERE (e.offered.id = :listingId OR e.selected.id = :listingId)
      AND e.status = 'PENDING'
""")
    boolean isListingInPendingExchange(@Param("listingId") Long listingId);

    @Query("""
    SELECT e FROM Exchange e
    WHERE (e.sender.id = :userId OR e.receiver.id = :userId)
      AND (:status IS NULL OR e.status = :status)
""")
    Page<Exchange> findAllByUserInvolvedAndOptionalStatus(@Param("userId") Long userId,
                                                          @Param("status") ExchangeStatus status,
                                                          Pageable pageable);
}

