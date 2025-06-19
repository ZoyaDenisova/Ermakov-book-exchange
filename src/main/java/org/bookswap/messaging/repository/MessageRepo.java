package org.bookswap.messaging.repository;

import org.bookswap.messaging.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MessageRepo extends JpaRepository<Message, Long> {

    // Все сообщения в диалоге по порядку
    Page<Message> findByDialogIdOrderByCreatedAtDesc(Long dialogId, Pageable pageable);

    // Последнее сообщение
    Optional<Message> findTop1ByDialogIdOrderByCreatedAtDesc(Long dialogId);
}
