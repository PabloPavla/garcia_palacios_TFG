-- ============================================================
-- V1__init_transfer.sql
-- Migración inicial del Transfer Service
-- Tablas: transfers – Trigger resolved_at + Stored Procedure
-- ============================================================

-- ── Tabla de transferencias ─────────────────────────────────
CREATE TABLE IF NOT EXISTS transfers (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id       BIGINT NOT NULL,      -- ID del jugador (club_db.players)
    from_club_id    BIGINT,               -- NULL si el jugador era agente libre
    to_club_id      BIGINT NOT NULL,      -- Club que realiza el fichaje
    transfer_fee    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status          ENUM('PENDING','ACCEPTED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    offered_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at     TIMESTAMP NULL,       -- Se rellena automáticamente por el trigger

    CONSTRAINT chk_transfer_fee  CHECK (transfer_fee >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices para consultas frecuentes
CREATE INDEX idx_transfers_player    ON transfers (player_id);
CREATE INDEX idx_transfers_to_club   ON transfers (to_club_id);
CREATE INDEX idx_transfers_from_club ON transfers (from_club_id);
CREATE INDEX idx_transfers_status    ON transfers (status);
CREATE INDEX idx_transfers_offered   ON transfers (offered_at DESC);

-- ============================================================
-- TRIGGER: Registrar fecha de resolución al cambiar el estado
-- Se dispara antes de cada UPDATE para mantener consistencia
-- ============================================================
DROP TRIGGER IF EXISTS trg_transfer_set_resolved_at;

DELIMITER //
CREATE TRIGGER trg_transfer_set_resolved_at
BEFORE UPDATE ON transfers
FOR EACH ROW
BEGIN
    -- Si el estado cambia de PENDING a un estado resuelto,
    -- registrar automáticamente la fecha y hora de resolución
    IF OLD.status = 'PENDING'
       AND NEW.status IN ('ACCEPTED', 'REJECTED', 'CANCELLED') THEN
        SET NEW.resolved_at = CURRENT_TIMESTAMP;
    END IF;

    -- Evitar reactivar una transferencia ya resuelta
    IF OLD.status != 'PENDING' AND NEW.status = 'PENDING' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'No se puede reactivar una transferencia ya resuelta';
    END IF;
END//
DELIMITER ;

-- ============================================================
-- PROCEDIMIENTO ALMACENADO: Cancelar transferencias pendientes
-- de un jugador cuando se acepta una nueva oferta
-- ============================================================
DROP PROCEDURE IF EXISTS sp_cancel_other_pending_transfers;

DELIMITER //
CREATE PROCEDURE sp_cancel_other_pending_transfers(
    IN p_player_id   BIGINT,
    IN p_accepted_id BIGINT
)
BEGIN
    -- Cancela todas las ofertas pendientes de un jugador
    -- excepto la que acaba de ser aceptada
    UPDATE transfers
    SET    status = 'CANCELLED'
    WHERE  player_id = p_player_id
      AND  id       != p_accepted_id
      AND  status    = 'PENDING';
END//
DELIMITER ;

-- ============================================================
-- PROCEDIMIENTO ALMACENADO: Estadísticas de fichajes por club
-- Devuelve resumen de transferencias de un club
-- ============================================================
DROP PROCEDURE IF EXISTS sp_club_transfer_stats;

DELIMITER //
CREATE PROCEDURE sp_club_transfer_stats(IN p_club_id BIGINT)
BEGIN
    SELECT
        SUM(CASE WHEN status = 'ACCEPTED' THEN 1 ELSE 0 END) AS total_aceptadas,
        SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) AS total_rechazadas,
        SUM(CASE WHEN status = 'PENDING'  THEN 1 ELSE 0 END) AS total_pendientes,
        COALESCE(SUM(CASE WHEN status = 'ACCEPTED' THEN transfer_fee ELSE 0 END), 0) AS gasto_total
    FROM transfers
    WHERE to_club_id = p_club_id;
END//
DELIMITER ;
