-- ============================================================
-- V1__init_club.sql
-- Migración inicial del Club Service
-- Tablas: clubs, players – Índices, constraints y triggers
-- ============================================================

-- ── Tabla de clubes ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS clubs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    acronym     VARCHAR(5)   NOT NULL,
    logo_url    VARCHAR(255),
    budget      DECIMAL(15,2) NOT NULL DEFAULT 1000000.00,
    owner_id    BIGINT NOT NULL,     -- ID del usuario propietario (auth_db.users)
    division    ENUM('BRONZE','SILVER','GOLD','PLATINUM','DIAMOND') NOT NULL DEFAULT 'BRONZE',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_club_name   UNIQUE (name),
    CONSTRAINT chk_budget     CHECK  (budget >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índice por propietario para listar clubes de un usuario
CREATE INDEX idx_clubs_owner    ON clubs (owner_id);
CREATE INDEX idx_clubs_division ON clubs (division);

-- ── Tabla de jugadores ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS players (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    summoner_name   VARCHAR(100) NOT NULL,
    real_name       VARCHAR(100),
    nationality     VARCHAR(50),
    age             INT,
    lol_role        ENUM('TOP','JUNGLE','MID','ADC','SUPPORT') NOT NULL,
    market_value    DECIMAL(15,2) NOT NULL DEFAULT 50000.00,
    current_club_id BIGINT,
    is_free_agent   TINYINT(1) NOT NULL DEFAULT 1,
    overall_rating  INT NOT NULL DEFAULT 70,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_summoner_name  UNIQUE  (summoner_name),
    CONSTRAINT chk_player_age    CHECK   (age IS NULL OR (age >= 16 AND age <= 50)),
    CONSTRAINT chk_rating        CHECK   (overall_rating BETWEEN 1 AND 99),
    CONSTRAINT chk_market_value  CHECK   (market_value >= 0),

    -- Un jugador sólo puede pertenecer a un club a la vez
    CONSTRAINT fk_player_club FOREIGN KEY (current_club_id)
        REFERENCES clubs (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices frecuentes
CREATE INDEX idx_players_club      ON players (current_club_id);
CREATE INDEX idx_players_role      ON players (lol_role);
CREATE INDEX idx_players_rating    ON players (overall_rating DESC);
-- Índice parcial simulado: solo indexa agentes libres (MySQL no soporta índices parciales directamente)
CREATE INDEX idx_players_free      ON players (is_free_agent);

-- ============================================================
-- TRIGGER: Limitar 2 jugadores por rol por club
-- Garantiza la integridad de la plantilla antes de cada INSERT
-- ============================================================
DROP TRIGGER IF EXISTS trg_check_role_limit_insert;

DELIMITER //
CREATE TRIGGER trg_check_role_limit_insert
BEFORE INSERT ON players
FOR EACH ROW
BEGIN
    DECLARE role_count INT DEFAULT 0;

    -- Solo verificar si el jugador va a un club (no si es agente libre)
    IF NEW.current_club_id IS NOT NULL THEN
        SELECT COUNT(*) INTO role_count
        FROM   players
        WHERE  current_club_id = NEW.current_club_id
          AND  lol_role        = NEW.lol_role;

        IF role_count >= 2 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El club ya tiene el máximo de 2 jugadores en ese rol';
        END IF;
    END IF;
END//
DELIMITER ;

-- TRIGGER: Mismo control al actualizar un jugador
DROP TRIGGER IF EXISTS trg_check_role_limit_update;

DELIMITER //
CREATE TRIGGER trg_check_role_limit_update
BEFORE UPDATE ON players
FOR EACH ROW
BEGIN
    DECLARE role_count INT DEFAULT 0;

    IF NEW.current_club_id IS NOT NULL
       AND (NEW.current_club_id != OLD.current_club_id OR NEW.lol_role != OLD.lol_role) THEN

        SELECT COUNT(*) INTO role_count
        FROM   players
        WHERE  current_club_id = NEW.current_club_id
          AND  lol_role        = NEW.lol_role
          AND  id             != NEW.id;   -- Excluir el propio jugador

        IF role_count >= 2 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El club ya tiene el máximo de 2 jugadores en ese rol';
        END IF;
    END IF;
END//
DELIMITER ;

-- ============================================================
-- FUNCIÓN: Obtener el valor de mercado ajustado por rating
-- ============================================================
DROP FUNCTION IF EXISTS fn_adjusted_market_value;

DELIMITER //
CREATE FUNCTION fn_adjusted_market_value(
    base_value   DECIMAL(15,2),
    rating       INT
) RETURNS DECIMAL(15,2)
DETERMINISTIC
BEGIN
    -- Ajuste lineal: rating 70 = valor base, rating 99 = 3× valor base
    RETURN base_value * (0.1 + (rating / 35.0));
END//
DELIMITER ;

-- ============================================================
-- Datos de ejemplo: jugadores de demostración
-- ============================================================
INSERT INTO players (summoner_name, real_name, nationality, age, lol_role, market_value, overall_rating, is_free_agent)
VALUES
('Faker',       'Lee Sang-hyeok', 'Coreano',  28, 'MID',     5000000.00, 99, 1),
('Caps',        'Rasmus Winther', 'Danés',     24, 'MID',     3000000.00, 93, 1),
('Jankos',      'Marcin Jankowski', 'Polaco',  30, 'JUNGLE',  2000000.00, 90, 1),
('Rekkles',     'Martin Larsson', 'Sueco',     28, 'ADC',     2500000.00, 92, 1),
('Mikyx',       'Mihael Mehle',   'Esloveno',  25, 'SUPPORT', 1800000.00, 89, 1),
('Cabochard',   'Gabriel Rau',    'Francés',   29, 'TOP',     1500000.00, 87, 1),
('Upset',       'Elias Lipp',     'Alemán',    23, 'ADC',     2800000.00, 94, 1),
('Hylissang',   'Zdravets Galabov','Búlgaro',  27, 'SUPPORT', 1600000.00, 88, 1),
('Razork',      'Iván Martín',    'Español',   24, 'JUNGLE',  1700000.00, 88, 1),
('humanoid',    'Marek Brázda',   'Checo',     24, 'MID',     2200000.00, 91, 1);
