package com.tfg.esports.league.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO para la creación de una nueva liga por un usuario.
 */
@Data
public class LeagueRequest {

    @NotBlank(message = "El nombre de la liga es obligatorio")
    private String name;

    @NotBlank(message = "La temporada es obligatoria")
    private String season;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Future(message = "La fecha de inicio debe ser en el futuro")
    private LocalDate startDate;

    @NotNull(message = "Los RP iniciales son obligatorios")
    @Min(value = 0, message = "Los RP iniciales no pueden ser negativos")
    private Integer initialRp;

    @NotNull(message = "El número máximo de clubes es obligatorio")
    @Min(value = 2, message = "Debe haber al menos 2 clubes")
    private Integer maxClubs;

    private String transferRules;

    @NotNull(message = "La apuesta por partido es obligatoria")
    @Min(value = 0, message = "La apuesta no puede ser negativa")
    private Integer matchWagerRp;

    private String visibility = "PUBLIC";
}
