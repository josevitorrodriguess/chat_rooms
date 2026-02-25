package com.josevitor.chatrooms.messages;

import com.josevitor.chatrooms.auth.JwtPrincipal;
import com.josevitor.chatrooms.messages.dto.MessagesPageResponse;
import com.josevitor.chatrooms.rooms.RoomMemberId;
import com.josevitor.chatrooms.rooms.RoomMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MessageQueryController {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private final MessageQueryService messageQueryService;
    private final RoomMemberRepository roomMemberRepository;

    public MessageQueryController(MessageQueryService messageQueryService,
                                  RoomMemberRepository roomMemberRepository) {
        this.messageQueryService = messageQueryService;
        this.roomMemberRepository = roomMemberRepository;
    }

    @GetMapping("/rooms/{id}/messages")
    public MessagesPageResponse list(@PathVariable Long id,
                                     @RequestParam(required = false) String cursor,
                                     @RequestParam(required = false) Integer limit,
                                     @AuthenticationPrincipal JwtPrincipal principal) {
        RoomMemberId memberId = new RoomMemberId(id, principal.userId());
        if (!roomMemberRepository.existsById(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not in room");
        }

        int pageSize = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        try {
            return messageQueryService.fetchPage(id, cursor, pageSize);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}
