-- V5__add_user_league_relationship.sql
-- Creación de la tabla league_members y adición de la columna owner_id a league_clubs

CREATE TABLE IF NOT EXISTS league_members (
    league_id BIGINT NOT NULL,
    user_id   BIGINT NOT NULL,
    PRIMARY KEY (league_id, user_id),
    CONSTRAINT fk_lm_league FOREIGN KEY (league_id) REFERENCES leagues (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE league_clubs ADD COLUMN owner_id BIGINT NOT NULL;
