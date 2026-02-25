package com.josevitor.chatrooms.rooms;

import com.josevitor.chatrooms.rooms.dto.CreateRoomRequest;
import com.josevitor.chatrooms.rooms.dto.RoomResponse;
import jakarta.persistence.EntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.List;

@Service
public class RoomService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int JOIN_CODE_LENGTH = 8;
    private static final int MAX_ATTEMPTS = 12;

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final PresenceService presenceService;
    private final EntityManager entityManager;
    private final SecureRandom random = new SecureRandom();

    public RoomService(RoomRepository roomRepository, RoomMemberRepository roomMemberRepository,
                       PresenceService presenceService,
                       EntityManager entityManager) {
        this.roomRepository = roomRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.presenceService = presenceService;
        this.entityManager = entityManager;
    }

    public RoomResponse create(CreateRoomRequest request, Long creatorId) {
        Room room = new Room();
        room.setName(request.name().trim());
        room.setMaxMembers(request.maxMembers());
        room.setCurrentMembers(0);
        room.setCreatorId(creatorId);
        room.setJoinCode(generateJoinCode());

        Room saved = roomRepository.save(room);
        return toResponse(saved);
    }

    public List<RoomResponse> list() {
        return roomRepository.findAll().stream().map(RoomService::toResponse).toList();
    }

    public RoomResponse getById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        return toResponse(room);
    }

    @Transactional
    public RoomResponse joinByCode(String joinCode, Long userId) {
        Room room = roomRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        RoomMemberId memberId = new RoomMemberId(room.getId(), userId);
        if (roomMemberRepository.existsById(memberId)) {
            presenceService.touchRoomMember(room.getId(), userId);
            Room refreshed = fetchFresh(room.getId(), room);
            return toResponse(refreshed);
        }

        int updated = roomRepository.incrementMembersByJoinCode(joinCode);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is full");
        }

        try {
            int inserted = roomMemberRepository.insertIfAbsent(room.getId(), userId);
            if (inserted == 0) {
                roomRepository.decrementMembersById(room.getId());
                presenceService.touchRoomMember(room.getId(), userId);
                Room refreshed = fetchFresh(room.getId(), room);
                return toResponse(refreshed);
            }
        } catch (DataIntegrityViolationException ex) {
            roomRepository.decrementMembersById(room.getId());
        }

        presenceService.touchRoomMember(room.getId(), userId);
        Room refreshed = fetchFresh(room.getId(), room);
        return toResponse(refreshed);
    }

    @Transactional
    public void leave(Long roomId, Long userId) {
        int deleted = roomMemberRepository.deleteByRoomIdAndUserId(roomId, userId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found");
        }
        roomRepository.decrementMembersById(roomId);
        presenceService.removeRoomMember(roomId, userId);
    }

    private String generateJoinCode() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = randomCode(JOIN_CODE_LENGTH);
            if (!roomRepository.existsByJoinCode(code)) {
                return code;
            }
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate join code");
    }

    private String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(CODE_CHARS.length());
            sb.append(CODE_CHARS.charAt(idx));
        }
        return sb.toString();
    }

    private Room fetchFresh(Long id, Room fallback) {
        entityManager.clear();
        return roomRepository.findById(id).orElse(fallback);
    }

    private static RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getJoinCode(),
                room.getMaxMembers(),
                room.getCurrentMembers(),
                room.getCreatorId(),
                room.getCreatedAt(),
                room.getExpiresAt()
        );
    }
}
