package com.josevitor.chatrooms.rooms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByJoinCode(String joinCode);

    Optional<Room> findByJoinCode(String joinCode);

    @Modifying
    @Query(value = """
            UPDATE rooms
            SET current_members = current_members + 1
            WHERE join_code = ?1
              AND current_members < max_members
            """, nativeQuery = true)
    int incrementMembersByJoinCode(String joinCode);

    @Modifying
    @Query(value = """
            UPDATE rooms
            SET current_members = current_members - 1
            WHERE id = ?1
              AND current_members > 0
            """, nativeQuery = true)
    int decrementMembersById(Long roomId);

    @Modifying
    @Query(value = """
            DELETE FROM rooms
            WHERE expires_at IS NOT NULL
              AND expires_at < ?1
            """, nativeQuery = true)
    int deleteExpiredRooms(Instant now);
}
