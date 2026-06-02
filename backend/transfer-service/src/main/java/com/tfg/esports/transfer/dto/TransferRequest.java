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

    /**
     * Cantidad ofrecida por el jugador en euros.
     * Puede ser 0 si el jugador es agente libre.
     */
    @NotNull(message = "La cantidad ofrecida es obligatoria")
    @DecimalMin(value = "0.0", message = "La oferta no puede ser negativa")
    private BigDecimal transferFee;
}
