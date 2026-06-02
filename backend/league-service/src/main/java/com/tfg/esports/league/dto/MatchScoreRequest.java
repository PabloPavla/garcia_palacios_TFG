package com.tfg.esports.league.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para registrar el resultado de un partido.
 *
 * @author Pablo García Palacios
 */
@Data
public class MatchScoreRequest {

    @NotNull(message = "Puntuación local obligatoria")
    @Min(0)
    private Integer homeScore;

    @NotNull(message = "Puntuación visitante obligatoria")
    @Min(0)
    private Integer awayScore;
}
