package org.bookswap.exchange.repository;

import org.bookswap.exchange.entity.Exchange;
import org.bookswap.exchange.entity.ExchangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExchangeRepo extends JpaRepository<Exchange, Long> {
    // Обмены, в которых участвует пользователь (как отправитель или получатель)
    @Query("""
    SELECT e FROM Exchange e
    WHERE e.sender.id = :userId OR e.receiver.id = :userId
    ORDER BY e.createdAt DESC
""")
    List<Exchange> findAllByUserInvolved(@Param("userId") Long userId);

    // Проверка, есть ли уже запрос обмена между двумя листингами (чтобы не дублировать)
    Optional<Exchange> findByOfferedIdAndSelectedId(Long offeredId, Long selectedId);

    // Все обмены по статусу (например, ожидающие подтверждения)
    List<Exchange> findByStatus(ExchangeStatus status);

    // Обмены, где листинг участвует (например, чтобы заблокировать участие в других при успешном)
    List<Exchange> findByOfferedIdOrSelectedId(Long listingId1, Long listingId2);

    // Проверить, участвует ли листинг в каком-то активном обмене
    @Query("""
    SELECT COUNT(e) > 0 FROM Exchange e
    WHERE (e.offered.id = :listingId OR e.selected.id = :listingId)
      AND e.status = 'PENDING'
""")
    boolean isListingInPendingExchange(@Param("listingId") Long listingId);

    //Найти активные обмены, ожидающие подтверждения с обеих сторон
    @Query("""
    SELECT e FROM Exchange e
    WHERE e.status = 'APPROVED'
      AND (e.senderConfirmedCompletion = false OR e.receiverConfirmedCompletion = false)
""")
    List<Exchange> findAwaitingFinalConfirmation();

    //Найти завершённые обмены пользователя
    @Query("""
    SELECT e FROM Exchange e
    WHERE (e.sender.id = :userId OR e.receiver.id = :userId)
      AND e.status = 'APPROVED'
      AND e.senderConfirmedCompletion = true
      AND e.receiverConfirmedCompletion = true
""")
    List<Exchange> findCompletedExchangesByUser(@Param("userId") Long userId);

    //Все обмены по листингу с конкретным статусом (напр. все отклонённые предложения по листингу)
    List<Exchange> findByOfferedIdAndStatus(Long offeredId, ExchangeStatus status);


}

