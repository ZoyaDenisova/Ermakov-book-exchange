package org.bookswap.messaging.repository;

import org.bookswap.messaging.entity.ChatImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatImageRepo extends JpaRepository<ChatImage, Long> {

    List<ChatImage> findByMessageId(Long messageId);

    void deleteByMessageId(Long messageId);
}
