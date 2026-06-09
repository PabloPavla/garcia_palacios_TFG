package com.tfg.esports.league.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entidad que representa una liga o temporada.
 *
 * @author Pablo García Palacios
 */
@Entity
@Table(
    name = "leagues",
    indexes = {
        @Index(name = "idx_leagues_season", columnList = "season"),
        @Index(name = "idx_leagues_active", columnList = "active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_league_season", columnNames = {"name", "season"})
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String season;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "initial_rp", nullable = false)
    @Builder.Default
    private Integer initialRp = 2000;

    @Column(name = "max_clubs", nullable = false)
    @Builder.Default
    private Integer maxClubs = 10;

    @Column(name = "transfer_rules")
    @Builder.Default
    private String transferRules = "OPEN";

    @Column(name = "match_wager_rp", nullable = false)
    @Builder.Default
    private Integer matchWagerRp = 500;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LeagueVisibility visibility = LeagueVisibility.PUBLIC;

    @Column(name = "creator_user_id")
    private Long creatorUserId;

    @Column(name = "winner_club_id")
    private Long winnerClubId;

    @Column(name = "winner_user_id")
    private Long winnerUserId;

    @Column(name = "invite_token", unique = true, length = 36)
    private String inviteToken;
}
