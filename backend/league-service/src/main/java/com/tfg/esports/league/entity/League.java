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
}
