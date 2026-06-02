package com.tfg.esports.transfer.dto;

import com.tfg.esports.transfer.entity.Transfer;
import com.tfg.esports.transfer.entity.TransferStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TransferResponse {

    private Long           id;
    private Long           playerId;
    private Long           fromClubId;
    private Long           toClubId;
    private Integer        transferFeeRp;
    private TransferStatus status;
    private LocalDateTime  offeredAt;
    private LocalDateTime  resolvedAt;

    private LocalDateTime  auctionEndTime;
    private Long           exchangePlayerId;
    private Integer        counterTransferFeeRp;
    private Long           counterExchangePlayerId;
    private Long           lastNegotiatorClubId;

    public static TransferResponse fromEntity(Transfer t) {
        return TransferResponse.builder()
                .id(t.getId())
                .playerId(t.getPlayerId())
                .fromClubId(t.getFromClubId())
                .toClubId(t.getToClubId())
                .transferFeeRp(t.getTransferFeeRp())
                .status(t.getStatus())
                .offeredAt(t.getOfferedAt())
                .resolvedAt(t.getResolvedAt())
                .auctionEndTime(t.getAuctionEndTime())
                .exchangePlayerId(t.getExchangePlayerId())
                .counterTransferFeeRp(t.getCounterTransferFeeRp())
                .counterExchangePlayerId(t.getCounterExchangePlayerId())
                .lastNegotiatorClubId(t.getLastNegotiatorClubId())
                .build();
    }
}
