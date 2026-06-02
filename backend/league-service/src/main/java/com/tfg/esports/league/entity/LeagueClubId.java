package com.tfg.esports.league.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Clave primaria compuesta para la relación entre Liga y Club.
 *
 * @author Pablo García Palacios
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LeagueClubId implements Serializable {

    @Column(name = "league_id")
    private Long leagueId;

    @Column(name = "club_id")
    private Long clubId;
}
