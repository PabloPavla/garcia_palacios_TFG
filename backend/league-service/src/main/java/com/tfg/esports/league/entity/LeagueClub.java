package com.tfg.esports.league.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa la participación de un club en una liga
 * y almacena sus estadísticas para la clasificación.
 *
 * <p>Nota: Los datos se actualizan mediante el trigger {@code trg_update_standings_after_match}.</p>
 *
 * @author Pablo García Palacios
 */
@Entity
@Table(
    name = "league_clubs",
    indexes = {
        @Index(name = "idx_lc_points", columnList = "league_id, points")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueClub {

    @EmbeddedId
    private LeagueClubId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("leagueId")
    @JoinColumn(name = "league_id")
    private League league;

    // clubId ya está en el LeagueClubId, pero podemos acceder a él

    @Column(nullable = false)
    @Builder.Default
    private Integer points = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer wins = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer losses = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer draws = 0;

    @Column(name = "goals_for", nullable = false)
    @Builder.Default
    private Integer goalsFor = 0;

    @Column(name = "goals_against", nullable = false)
    @Builder.Default
    private Integer goalsAgainst = 0;
}
