-- ============================================================
-- V6__add_league_winner.sql
-- Añade columnas para almacenar el club y usuario ganadores
-- ============================================================

ALTER TABLE leagues ADD COLUMN winner_club_id BIGINT DEFAULT NULL;
ALTER TABLE leagues ADD COLUMN winner_user_id BIGINT DEFAULT NULL;
