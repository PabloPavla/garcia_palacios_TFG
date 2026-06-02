-- V3__advanced_transfers.sql
-- Add columns for auction and swaps

-- Modify ENUM to include new statuses
ALTER TABLE transfers 
MODIFY COLUMN status ENUM('PENDING','ACCEPTED','REJECTED','CANCELLED','AUCTION','COUNTER_OFFERED') NOT NULL DEFAULT 'PENDING';

-- Add new columns
ALTER TABLE transfers
ADD COLUMN auction_end_time DATETIME NULL,
ADD COLUMN exchange_player_id BIGINT NULL,
ADD COLUMN counter_transfer_fee_rp INT NULL,
ADD COLUMN counter_exchange_player_id BIGINT NULL,
ADD COLUMN last_negotiator_club_id BIGINT NULL;

-- Drop and recreate the trigger to handle the new statuses properly
DROP TRIGGER IF EXISTS trg_transfer_set_resolved_at;

DELIMITER //
CREATE TRIGGER trg_transfer_set_resolved_at
BEFORE UPDATE ON transfers
FOR EACH ROW
BEGIN
    -- If status changes to a resolved state, set resolved_at
    IF OLD.status IN ('PENDING', 'AUCTION', 'COUNTER_OFFERED')
       AND NEW.status IN ('ACCEPTED', 'REJECTED', 'CANCELLED') THEN
        SET NEW.resolved_at = CURRENT_TIMESTAMP;
    END IF;

    -- Cannot reactivate a resolved transfer
    IF OLD.status IN ('ACCEPTED', 'REJECTED', 'CANCELLED') AND NEW.status IN ('PENDING', 'AUCTION', 'COUNTER_OFFERED') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'No se puede reactivar una transferencia ya resuelta';
    END IF;
END//
DELIMITER ;
