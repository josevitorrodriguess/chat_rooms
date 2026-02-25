package com.josevitor.chatrooms.messages;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query(value = """
            SELECT * FROM messages
            WHERE room_id = ?1
            ORDER BY created_at DESC, id DESC
            LIMIT ?2
            """, nativeQuery = true)
    List<Message> findPage(Long roomId, int limit);

    @Query(value = """
            SELECT * FROM messages
            WHERE room_id = ?1
              AND (created_at, id) < (?2, ?3)
            ORDER BY created_at DESC, id DESC
            LIMIT ?4
            """, nativeQuery = true)
    List<Message> findPageBefore(Long roomId, Instant createdAt, Long id, int limit);
}
