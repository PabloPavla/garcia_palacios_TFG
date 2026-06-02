-- V3__add_league_visibility.sql
-- Añadir campos de visibilidad a las ligas

ALTER TABLE leagues ADD COLUMN visibility ENUM('PUBLIC','PRIVATE','FRIENDS_ONLY') NOT NULL DEFAULT 'PUBLIC';
ALTER TABLE leagues ADD COLUMN creator_user_id BIGINT NULL;
