package com.josevitor.chatrooms.rooms;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "room_members")
public class RoomMember {

    @EmbeddedId
    private RoomMemberId id;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    protected RoomMember() {
    }

    public RoomMember(RoomMemberId id) {
        this.id = id;
    }

    @PrePersist
    void onCreate() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }

    public RoomMemberId getId() {
        return id;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
