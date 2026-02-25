package com.josevitor.chatrooms.rooms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RoomMemberRepository extends JpaRepository<RoomMember, RoomMemberId> {
    boolean existsById(RoomMemberId id);

    @Modifying
    @Query(value = """
            DELETE FROM room_members
            WHERE room_id = ?1 AND user_id = ?2
            """, nativeQuery = true)
    int deleteByRoomIdAndUserId(Long roomId, Long userId);

    @Modifying
    @Query(value = """
            INSERT INTO room_members (room_id, user_id)
            VALUES (?1, ?2)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(Long roomId, Long userId);
}
