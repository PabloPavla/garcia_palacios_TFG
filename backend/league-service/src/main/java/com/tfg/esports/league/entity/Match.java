package com.tfg.esports.league.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un partido entre dos clubes en una liga.
 *
 * @author Pablo García Palacios
 */
@Entity
@Table(
    name = "matches",
    indexes = {
        @Index(name = "idx_matches_league", columnList = "league_id"),
        @Index(name = "idx_matches_date",   columnList = "match_date"),
        @Index(name = "idx_matches_status", columnList = "status")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @Column(name = "home_club_id", nullable = false)
    private Long homeClubId;

    @Column(name = "away_club_id", nullable = false)
    private Long awayClubId;

    @Column(name = "match_date", nullable = false)
    private LocalDateTime matchDate;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MatchStatus status = MatchStatus.SCHEDULED;
}
