-- ============================================================
-- V7__add_league_invitations.sql
-- Añadir columna de token de invitación a ligas y tabla de invitaciones
-- ============================================================

ALTER TABLE leagues ADD COLUMN invite_token VARCHAR(36) UNIQUE DEFAULT NULL;
UPDATE leagues SET invite_token = UUID() WHERE invite_token IS NULL;

CREATE TABLE IF NOT EXISTS league_invitations (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    league_id   BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    status      VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invitation_league FOREIGN KEY (league_id) REFERENCES leagues (id) ON DELETE CASCADE,
    CONSTRAINT uq_league_user_invitation UNIQUE (league_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
