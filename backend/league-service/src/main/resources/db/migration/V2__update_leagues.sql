-- V2__update_leagues.sql
-- Transición a economía de Riot Points y Ligas Personalizadas

-- 1. Añadir configuración a la tabla leagues
ALTER TABLE leagues ADD COLUMN initial_rp INT NOT NULL DEFAULT 10000;
ALTER TABLE leagues ADD COLUMN max_clubs INT NOT NULL DEFAULT 10;
ALTER TABLE leagues ADD COLUMN transfer_rules VARCHAR(255) DEFAULT 'OPEN';
ALTER TABLE leagues ADD COLUMN match_wager_rp INT NOT NULL DEFAULT 500;

-- 2. Añadir apuesta por partido a la tabla matches
ALTER TABLE matches ADD COLUMN wager_rp INT NOT NULL DEFAULT 500;

-- 3. Limpiar liga de ejemplo (ya que ahora los usuarios las crean con configuración)
DELETE FROM leagues;
