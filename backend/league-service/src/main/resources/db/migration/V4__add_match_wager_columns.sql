-- Add columns to support match wagers and tournament brackets
ALTER TABLE matches
    ADD COLUMN home_wager_accepted BOOLEAN DEFAULT FALSE,
    ADD COLUMN away_wager_accepted BOOLEAN DEFAULT FALSE,
    ADD COLUMN tournament_round VARCHAR(50);
