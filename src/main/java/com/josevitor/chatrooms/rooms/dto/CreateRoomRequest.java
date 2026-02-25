package com.josevitor.chatrooms.rooms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
        @NotBlank
        @Size(max = 80)
        String name,
        @Min(1)
        @Max(1000)
        int maxMembers
) {
}
