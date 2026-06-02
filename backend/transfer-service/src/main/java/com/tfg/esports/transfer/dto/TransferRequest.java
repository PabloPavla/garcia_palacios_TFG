package com.tfg.esports.transfer.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de solicitud para crear una nueva oferta de transferencia.
 *
 * <p>El club comprador (identificado por el Gateway) envía una oferta
 * por un jugador concreto, indicando la cantidad que ofrece.</p>
 *
 * @author Pablo García Palacios
 */
@Data
public class TransferRequest {

    /** ID del jugador al que se hace la oferta */
    @NotNull(message = "El ID del jugador es obligatorio")
    private Long playerId;

    /** Oferta económica en Riot Points */
    @NotNull(message = "La oferta económica es obligatoria")
    @Min(value = 0, message = "La oferta no puede ser negativa")
    private Integer transferFeeRp;
}
