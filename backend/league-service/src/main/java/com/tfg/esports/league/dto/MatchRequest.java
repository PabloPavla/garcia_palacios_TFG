package com.tfg.esports.league.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para programar un nuevo partido.
 *
 * @author Pablo García Palacios
 */
@Data
public class MatchRequest {

    @NotNull(message = "El ID de la liga es obligatorio")
    private Long leagueId;

    @NotNull(message = "El club local es obligatorio")
    private Long homeClubId;

    @NotNull(message = "El club visitante es obligatorio")
    private Long awayClubId;

    @NotNull(message = "La fecha del partido es obligatoria")
    @Future(message = "La fecha del partido debe ser futura")
    private LocalDateTime matchDate;
}
