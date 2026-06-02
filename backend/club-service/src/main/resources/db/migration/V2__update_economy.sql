-- V2__update_economy.sql
-- Transición a economía de Riot Points y Ligas Independientes

-- 1. Limpiar datos de demostración anteriores
DELETE FROM players;
DELETE FROM clubs;

-- 2. Actualizar Clubs (budget -> riot_points)
ALTER TABLE clubs DROP CHECK chk_budget;
ALTER TABLE clubs CHANGE budget riot_points INT NOT NULL DEFAULT 10000;
ALTER TABLE clubs ADD CONSTRAINT chk_riot_points CHECK (riot_points >= 0);

-- 3. Actualizar Players (market_value -> price_rp, y añadir league_id)
ALTER TABLE players DROP CHECK chk_market_value;
ALTER TABLE players CHANGE market_value price_rp INT NOT NULL DEFAULT 500;
ALTER TABLE players ADD CONSTRAINT chk_price_rp CHECK (price_rp >= 0);

-- Añadimos league_id para aislar a los jugadores por liga
ALTER TABLE players ADD COLUMN league_id BIGINT NOT NULL;
CREATE INDEX idx_players_league ON players (league_id);

-- 4. Reemplazar función de ajuste de precio para usar INT
DROP FUNCTION IF EXISTS fn_adjusted_market_value;

DELIMITER //
CREATE FUNCTION fn_adjusted_market_value(
    base_value   INT,
    rating       INT
) RETURNS INT
DETERMINISTIC
BEGIN
    -- Ajuste lineal: rating 70 = valor base, rating 99 = 3× valor base
    RETURN CAST(base_value * (0.1 + (rating / 35.0)) AS UNSIGNED);
END//
DELIMITER ;
