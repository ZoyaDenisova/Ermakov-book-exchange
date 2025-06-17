package org.bookswap.messaging.repository;

import org.bookswap.messaging.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepo extends JpaRepository<Message, Long> {

    // Все сообщения в диалоге по порядку
    List<Message> findByDialogIdOrderByCreatedAtAsc(Long dialogId);

    // Последнее сообщение
    Optional<Message> findTop1ByDialogIdOrderByCreatedAtDesc(Long dialogId);
}
