package com.josevitor.chatrooms.rooms;

import com.josevitor.chatrooms.auth.JwtPrincipal;
import com.josevitor.chatrooms.rooms.dto.CreateRoomRequest;
import com.josevitor.chatrooms.rooms.dto.JoinRoomRequest;
import com.josevitor.chatrooms.rooms.dto.RoomResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public RoomResponse create(@Valid @RequestBody CreateRoomRequest request,
                               @AuthenticationPrincipal JwtPrincipal principal) {
        return roomService.create(request, principal.userId());
    }

    @GetMapping
    public List<RoomResponse> list() {
        return roomService.list();
    }

    @GetMapping("/{id}")
    public RoomResponse getById(@PathVariable Long id) {
        return roomService.getById(id);
    }

    @PostMapping("/join")
    public RoomResponse join(@Valid @RequestBody JoinRoomRequest request,
                             @AuthenticationPrincipal JwtPrincipal principal) {
        return roomService.joinByCode(request.joinCode().trim(), principal.userId());
    }

    @PostMapping("/{id}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@PathVariable Long id, @AuthenticationPrincipal JwtPrincipal principal) {
        roomService.leave(id, principal.userId());
    }
}
