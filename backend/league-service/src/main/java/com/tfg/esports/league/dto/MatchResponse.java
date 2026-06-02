package com.tfg.esports.league.dto;

import com.tfg.esports.league.entity.Match;
import com.tfg.esports.league.entity.MatchStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para un partido.
 *
 * @author Pablo García Palacios
 */
@Data
@Builder
public class MatchResponse {
    private Long id;
    private Long leagueId;
    private Long homeClubId;
    private Long awayClubId;
    private LocalDateTime matchDate;
    private Integer homeScore;
    private Integer awayScore;
    private MatchStatus status;

    public static MatchResponse fromEntity(Match m) {
        return MatchResponse.builder()
                .id(m.getId())
                .leagueId(m.getLeague().getId())
                .homeClubId(m.getHomeClubId())
                .awayClubId(m.getAwayClubId())
                .matchDate(m.getMatchDate())
                .homeScore(m.getHomeScore())
                .awayScore(m.getAwayScore())
                .status(m.getStatus())
                .build();
    }
}
