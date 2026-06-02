package com.tfg.esports.transfer.dto;

import com.tfg.esports.transfer.entity.Transfer;
import com.tfg.esports.transfer.entity.TransferStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta con los datos completos de una transferencia.
 *
 * @author Pablo García Palacios
 */
@Data
@Builder
public class TransferResponse {

    private Long           id;
    private Long           playerId;
    private Long           fromClubId;
    private Long           toClubId;
    private BigDecimal     transferFee;
    private TransferStatus status;
    private LocalDateTime  offeredAt;
    private LocalDateTime  resolvedAt;

    /**
     * Convierte una entidad {@link Transfer} a este DTO.
     *
     * @param t la entidad de transferencia
     * @return DTO con los datos de la transferencia
     */
    public static TransferResponse fromEntity(Transfer t) {
        return TransferResponse.builder()
                .id(t.getId())
                .playerId(t.getPlayerId())
                .fromClubId(t.getFromClubId())
                .toClubId(t.getToClubId())
                .transferFee(t.getTransferFee())
                .status(t.getStatus())
                .offeredAt(t.getOfferedAt())
                .resolvedAt(t.getResolvedAt())
                .build();
    }
}
