-- ============================================================
-- V1__init_league.sql
-- Migración inicial del League Service
-- Tablas: leagues, league_clubs, matches – Trigger clasificación
-- ============================================================

-- ── Tabla de ligas / temporadas ─────────────────────────────
CREATE TABLE IF NOT EXISTS leagues (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    season      VARCHAR(20)  NOT NULL,   -- p. ej. "2025-S1"
    start_date  DATE NOT NULL,
    end_date    DATE,
    active      TINYINT(1) NOT NULL DEFAULT 1,

    CONSTRAINT uq_league_season UNIQUE (name, season)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_leagues_season ON leagues (season);
CREATE INDEX idx_leagues_active ON leagues (active);

-- ── Tabla de participantes en liga (clasificación) ──────────
CREATE TABLE IF NOT EXISTS league_clubs (
    league_id   BIGINT NOT NULL,
    club_id     BIGINT NOT NULL,    -- ID del club (club_db.clubs)
    points      INT NOT NULL DEFAULT 0,
    wins        INT NOT NULL DEFAULT 0,
    losses      INT NOT NULL DEFAULT 0,
    draws       INT NOT NULL DEFAULT 0,
    goals_for   INT NOT NULL DEFAULT 0,
    goals_against INT NOT NULL DEFAULT 0,

    PRIMARY KEY (league_id, club_id),
    CONSTRAINT fk_lc_league FOREIGN KEY (league_id)
        REFERENCES leagues (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_lc_points ON league_clubs (league_id, points DESC);

-- ── Tabla de partidos ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS matches (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    league_id       BIGINT NOT NULL,
    home_club_id    BIGINT NOT NULL,    -- Club local (club_db.clubs)
    away_club_id    BIGINT NOT NULL,    -- Club visitante (club_db.clubs)
    match_date      TIMESTAMP NOT NULL,
    home_score      INT DEFAULT NULL,   -- NULL hasta que se juegue
    away_score      INT DEFAULT NULL,
    status          ENUM('SCHEDULED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'SCHEDULED',

    CONSTRAINT chk_different_clubs CHECK (home_club_id != away_club_id),
    CONSTRAINT chk_scores          CHECK (
        (status = 'COMPLETED' AND home_score IS NOT NULL AND away_score IS NOT NULL)
        OR status != 'COMPLETED'
    ),
    CONSTRAINT fk_match_league FOREIGN KEY (league_id)
        REFERENCES leagues (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_matches_league ON matches (league_id);
CREATE INDEX idx_matches_date   ON matches (match_date DESC);
CREATE INDEX idx_matches_status ON matches (status);

-- ============================================================
-- TRIGGER: Actualizar la clasificación al registrar un resultado
-- Se dispara AFTER UPDATE cuando el partido cambia a COMPLETED
-- ============================================================
DROP TRIGGER IF EXISTS trg_update_standings_after_match;

DELIMITER //
CREATE TRIGGER trg_update_standings_after_match
AFTER UPDATE ON matches
FOR EACH ROW
BEGIN
    -- Solo actuar cuando el partido pasa de cualquier estado a COMPLETED
    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN

        -- ── Victoria del equipo LOCAL ──────────────────────
        IF NEW.home_score > NEW.away_score THEN
            -- Local: +3 puntos, +1 victoria
            UPDATE league_clubs
            SET points = points + 3,
                wins   = wins   + 1,
                goals_for     = goals_for     + NEW.home_score,
                goals_against = goals_against + NEW.away_score
            WHERE league_id = NEW.league_id AND club_id = NEW.home_club_id;

            -- Visitante: +0 puntos, +1 derrota
            UPDATE league_clubs
            SET losses = losses + 1,
                goals_for     = goals_for     + NEW.away_score,
                goals_against = goals_against + NEW.home_score
            WHERE league_id = NEW.league_id AND club_id = NEW.away_club_id;

        -- ── Victoria del equipo VISITANTE ──────────────────
        ELSEIF NEW.away_score > NEW.home_score THEN
            -- Visitante: +3 puntos, +1 victoria
            UPDATE league_clubs
            SET points = points + 3,
                wins   = wins   + 1,
                goals_for     = goals_for     + NEW.away_score,
                goals_against = goals_against + NEW.home_score
            WHERE league_id = NEW.league_id AND club_id = NEW.away_club_id;

            -- Local: +0 puntos, +1 derrota
            UPDATE league_clubs
            SET losses = losses + 1,
                goals_for     = goals_for     + NEW.home_score,
                goals_against = goals_against + NEW.away_score
            WHERE league_id = NEW.league_id AND club_id = NEW.home_club_id;

        -- ── EMPATE ─────────────────────────────────────────
        ELSE
            -- Ambos: +1 punto, +1 empate
            UPDATE league_clubs
            SET points = points + 1,
                draws  = draws  + 1,
                goals_for     = goals_for     + NEW.home_score,
                goals_against = goals_against + NEW.away_score
            WHERE league_id = NEW.league_id AND club_id = NEW.home_club_id;

            UPDATE league_clubs
            SET points = points + 1,
                draws  = draws  + 1,
                goals_for     = goals_for     + NEW.away_score,
                goals_against = goals_against + NEW.home_score
            WHERE league_id = NEW.league_id AND club_id = NEW.away_club_id;
        END IF;

    END IF;
END//
DELIMITER ;

-- ============================================================
-- PROCEDIMIENTO: Obtener clasificación ordenada de una liga
-- ============================================================
DROP PROCEDURE IF EXISTS sp_get_standings;

DELIMITER //
CREATE PROCEDURE sp_get_standings(IN p_league_id BIGINT)
BEGIN
    SELECT
        lc.club_id,
        lc.points,
        lc.wins,
        lc.draws,
        lc.losses,
        lc.goals_for,
        lc.goals_against,
        (lc.goals_for - lc.goals_against) AS goal_difference,
        (lc.wins + lc.draws + lc.losses)  AS games_played
    FROM league_clubs lc
    WHERE lc.league_id = p_league_id
    ORDER BY
        lc.points       DESC,
        goal_difference DESC,
        lc.goals_for    DESC;
END//
DELIMITER ;

-- ============================================================
-- Liga de ejemplo para desarrollo
-- ============================================================
INSERT INTO leagues (name, season, start_date, active)
VALUES ('LEC TFG League', '2025-S1', '2025-09-01', 1);
