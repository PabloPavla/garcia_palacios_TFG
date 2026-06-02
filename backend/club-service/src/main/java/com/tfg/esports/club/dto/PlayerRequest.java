package com.tfg.esports.club.dto;

import com.tfg.esports.club.entity.LolRole;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de solicitud para crear o actualizar un jugador.
 *
 * @author Pablo García Palacios
 */
@Data
public class PlayerRequest {

    /** Nombre de invocador único */
    @NotBlank(message = "El nombre de invocador es obligatorio")
    @Size(max = 100, message = "El summoner name no puede superar 100 caracteres")
    private String summonerName;

    /** Nombre real del jugador (opcional) */
    @Size(max = 100)
    private String realName;

    /** Nacionalidad (opcional) */
    @Size(max = 50)
    private String nationality;

    /** Edad (entre 16 y 50) */
    @Min(value = 16, message = "La edad mínima es 16 años")
    @Max(value = 50, message = "La edad máxima es 50 años")
    private Integer age;

    /** Rol principal en LoL */
    @NotNull(message = "El rol es obligatorio")
    private LolRole lolRole;

    /** Valor de mercado en euros (mínimo 0) */
    @DecimalMin(value = "0.0", message = "El valor de mercado no puede ser negativo")
    private BigDecimal marketValue;

    /** Valoración general (1–99) */
    @Min(value = 1, message = "La valoración mínima es 1")
    @Max(value = 99, message = "La valoración máxima es 99")
    private Integer overallRating;
}
