package com.tfg.esports.transfer.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TransferRequest {

    @NotNull(message = "El ID del jugador es obligatorio")
    private Long playerId;

    @NotNull(message = "El ID del club comprador es obligatorio")
    private Long toClubId;

    @NotNull(message = "La oferta económica es obligatoria")
    @Min(value = 0, message = "La oferta no puede ser negativa")
    private Integer transferFeeRp;

    private Long exchangePlayerId;
}
