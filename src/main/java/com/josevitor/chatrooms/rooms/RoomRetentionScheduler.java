package com.josevitor.chatrooms.rooms;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class RoomRetentionScheduler {

    private final RoomRepository roomRepository;

    public RoomRetentionScheduler(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    // Runs daily at 03:00 server time
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpiredRooms() {
        roomRepository.deleteExpiredRooms(Instant.now());
    }
}
