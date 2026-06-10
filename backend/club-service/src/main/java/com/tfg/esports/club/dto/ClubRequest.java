package com.tfg.esports.club.dto;

import com.tfg.esports.club.entity.Division;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de solicitud para crear o actualizar un club.
 *
 * @author Pablo García Palacios
 */
@Data
public class ClubRequest {

    /** Nombre completo del club */
    @NotBlank(message = "El nombre del club es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String name;

    /** Acrónimo del club (2-5 caracteres) */
    @NotBlank(message = "El acrónimo es obligatorio")
    @Size(min = 2, max = 5, message = "El acrónimo debe tener entre 2 y 5 caracteres")
    private String acronym;

    /** URL del logo (opcional) */
    private String logoUrl;

    /** División inicial del club (por defecto BRONZE) */
    private Division division;

    /** RP iniciales que se asignarán al club (opcional) */
    private Integer initialRp;
}
