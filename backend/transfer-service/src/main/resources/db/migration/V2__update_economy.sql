-- V2__update_economy.sql
-- Transición a economía de Riot Points en Transfer Service

-- 1. Limpiar datos de transferencias anteriores
DELETE FROM transfers;

-- 2. Cambiar transfer_fee a transfer_fee_rp (INT)
ALTER TABLE transfers DROP CHECK chk_transfer_fee;
ALTER TABLE transfers CHANGE transfer_fee transfer_fee_rp INT NOT NULL DEFAULT 0;
ALTER TABLE transfers ADD CONSTRAINT chk_transfer_fee_rp CHECK (transfer_fee_rp >= 0);

-- 3. Actualizar Procedimiento Almacenado de Estadísticas
DROP PROCEDURE IF EXISTS sp_club_transfer_stats;

DELIMITER //
CREATE PROCEDURE sp_club_transfer_stats(IN p_club_id BIGINT)
BEGIN
    SELECT
        SUM(CASE WHEN status = 'ACCEPTED' THEN 1 ELSE 0 END) AS total_aceptadas,
        SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) AS total_rechazadas,
        SUM(CASE WHEN status = 'PENDING'  THEN 1 ELSE 0 END) AS total_pendientes,
        COALESCE(SUM(CASE WHEN status = 'ACCEPTED' THEN transfer_fee_rp ELSE 0 END), 0) AS gasto_total_rp
    FROM transfers
    WHERE to_club_id = p_club_id;
END//
DELIMITER ;
