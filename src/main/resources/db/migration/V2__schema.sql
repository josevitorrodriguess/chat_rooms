-- V2__schema.sql

-- USERS
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
     username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
    );

-- ROOMS
CREATE TABLE IF NOT EXISTS rooms (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(80) NOT NULL,
    join_code       VARCHAR(12) NOT NULL UNIQUE,
    max_members     INT         NOT NULL CHECK (max_members > 0),
    current_members INT         NOT NULL DEFAULT 0 CHECK (current_members >= 0),
    creator_id      BIGINT      NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at      TIMESTAMPTZ  NULL
    );

-- ROOM MEMBERS (membership)
CREATE TABLE IF NOT EXISTS room_members (
    room_id   BIGINT     NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    user_id   BIGINT     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (room_id, user_id)
    );

-- MESSAGES
CREATE TABLE IF NOT EXISTS messages (
    id                BIGSERIAL PRIMARY KEY,
   room_id           BIGINT      NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    sender_id         BIGINT      NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    client_message_id UUID        NOT NULL,
    content           TEXT        NOT NULL CHECK (char_length(content) <= 2000),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_message_dedup UNIQUE (sender_id, client_message_id)
    );

-- INDEXES
CREATE INDEX IF NOT EXISTS idx_room_members_room_id ON room_members(room_id);
CREATE INDEX IF NOT EXISTS idx_messages_room_id_created_at ON messages(room_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_rooms_join_code ON rooms(join_code);