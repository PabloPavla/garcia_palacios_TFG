package com.tfg.esports.league.dto;

import com.tfg.esports.league.entity.LeagueClub;
import lombok.Builder;
import lombok.Data;

/**
 * DTO que representa una fila en la tabla de clasificación.
 *
 * @author Pablo García Palacios
 */
@Data
@Builder
public class StandingResponse {
    private Long clubId;
    private int points;
    private int wins;
    private int draws;
    private int losses;
    private int goalsFor;
    private int goalsAgainst;
    private int goalDifference;
    private int gamesPlayed;

    public static StandingResponse fromEntity(LeagueClub lc) {
        return StandingResponse.builder()
                .clubId(lc.getId().getClubId())
                .points(lc.getPoints())
                .wins(lc.getWins())
                .draws(lc.getDraws())
                .losses(lc.getLosses())
                .goalsFor(lc.getGoalsFor())
                .goalsAgainst(lc.getGoalsAgainst())
                .goalDifference(lc.getGoalsFor() - lc.getGoalsAgainst())
                .gamesPlayed(lc.getWins() + lc.getDraws() + lc.getLosses())
                .build();
    }
}
