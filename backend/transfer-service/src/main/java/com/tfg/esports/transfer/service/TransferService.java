package com.tfg.esports.transfer.service;

import com.tfg.esports.transfer.client.ClubClient;
import com.tfg.esports.transfer.dto.TransferRequest;
import com.tfg.esports.transfer.dto.TransferResponse;
import com.tfg.esports.transfer.entity.Transfer;
import com.tfg.esports.transfer.entity.TransferStatus;
import com.tfg.esports.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final ClubClient clubClient;

    @Transactional(readOnly = true)
    public Page<TransferResponse> getAllTransfers(Pageable pageable) {
        return transferRepository.findAll(pageable).map(TransferResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public TransferResponse getTransferById(Long id) {
        return TransferResponse.fromEntity(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> getTransfersByBuyingClub(Long clubId, Pageable pageable) {
        return transferRepository.findByToClubId(clubId, pageable).map(TransferResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> getTransfersBySellingClub(Long clubId, Pageable pageable) {
        return transferRepository.findByFromClubId(clubId, pageable).map(TransferResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> getTransfersByPlayer(Long playerId) {
        return transferRepository.findByPlayerIdOrderByOfferedAtDesc(playerId)
                .stream()
                .map(TransferResponse::fromEntity)
                .toList();
    }

    @Transactional
    public TransferResponse createTransfer(TransferRequest request, Long toClubId) {
        Long ownerClubId = clubClient.getPlayerOwnerClubId(request.getPlayerId());
        
        if (ownerClubId == null) {
            // Free Agent -> Auction
            return handleAuctionBid(request.getPlayerId(), toClubId, request.getTransferFeeRp());
        } else {
            // Owned Player -> Direct Transfer Offer
            if (ownerClubId.equals(toClubId)) {
                throw new IllegalArgumentException("Ya posees a este jugador.");
            }
            
            // Validate pending offers
            List<Transfer> pendingOffers = transferRepository
                    .findByPlayerIdAndStatus(request.getPlayerId(), TransferStatus.PENDING);
            boolean alreadyOffered = pendingOffers.stream()
                    .anyMatch(t -> t.getToClubId().equals(toClubId));
            if (alreadyOffered) {
                throw new IllegalArgumentException("Ya tienes una oferta pendiente por este jugador");
            }

            Transfer transfer = Transfer.builder()
                    .playerId(request.getPlayerId())
                    .fromClubId(ownerClubId)
                    .toClubId(toClubId)
                    .transferFeeRp(request.getTransferFeeRp())
                    .exchangePlayerId(request.getExchangePlayerId())
                    .status(TransferStatus.PENDING)
                    .lastNegotiatorClubId(toClubId)
                    .build();

            // We do NOT deduct RP here yet, we wait until acceptance
            return TransferResponse.fromEntity(transferRepository.save(transfer));
        }
    }

    @Transactional
    public TransferResponse counterOffer(Long id, TransferRequest request, Long clubId) {
        Transfer transfer = findOrThrow(id);
        if (transfer.getStatus() != TransferStatus.PENDING && transfer.getStatus() != TransferStatus.COUNTER_OFFERED) {
            throw new IllegalArgumentException("Solo se pueden contraofertar transferencias en curso.");
        }
        
        // Ensure the counter offer comes from the other party
        if (transfer.getLastNegotiatorClubId().equals(clubId)) {
            throw new IllegalArgumentException("Es el turno del otro club para responder.");
        }
        
        transfer.setStatus(TransferStatus.COUNTER_OFFERED);
        transfer.setCounterTransferFeeRp(request.getTransferFeeRp());
        transfer.setCounterExchangePlayerId(request.getExchangePlayerId());
        transfer.setLastNegotiatorClubId(clubId);
        
        return TransferResponse.fromEntity(transferRepository.save(transfer));
    }

    @Transactional
    public TransferResponse acceptTransfer(Long id, Long clubId) {
        Transfer transfer = findOrThrow(id);
        if (transfer.getStatus() != TransferStatus.PENDING && transfer.getStatus() != TransferStatus.COUNTER_OFFERED) {
            throw new IllegalArgumentException("Solo se pueden aceptar transferencias en curso.");
        }
        
        // Ensure the accepting party is NOT the last negotiator
        if (transfer.getLastNegotiatorClubId().equals(clubId)) {
            throw new IllegalArgumentException("No puedes aceptar tu propia oferta.");
        }
        
        int finalRp = transfer.getStatus() == TransferStatus.COUNTER_OFFERED ? 
                (transfer.getCounterTransferFeeRp() != null ? transfer.getCounterTransferFeeRp() : 0) : 
                (transfer.getTransferFeeRp() != null ? transfer.getTransferFeeRp() : 0);
                
        Long finalExchange = transfer.getStatus() == TransferStatus.COUNTER_OFFERED ? 
                transfer.getCounterExchangePlayerId() : transfer.getExchangePlayerId();

        // buyer pays RP, seller gets RP
        // If clubId == toClubId, then buyer accepted seller's counter-offer
        // If clubId == fromClubId, then seller accepted buyer's offer
        
        // buyer is toClubId
        clubClient.deductRp(transfer.getToClubId(), finalRp);
        clubClient.addRp(transfer.getFromClubId(), finalRp);
        
        if (finalExchange != null) {
            // Swap players
            clubClient.swapPlayers(transfer.getPlayerId(), finalExchange);
        } else {
            // Transfer single player
            clubClient.transferPlayer(transfer.getPlayerId(), transfer.getToClubId());
        }

        transfer.setStatus(TransferStatus.ACCEPTED);
        transfer.setResolvedAt(LocalDateTime.now());
        Transfer saved = transferRepository.save(transfer);
        transferRepository.cancelOtherPendingTransfers(transfer.getPlayerId(), id);

        return TransferResponse.fromEntity(saved);
    }

    @Transactional
    public TransferResponse rejectTransfer(Long id, Long clubId) {
        Transfer transfer = findOrThrow(id);
        if (transfer.getStatus() != TransferStatus.PENDING && transfer.getStatus() != TransferStatus.COUNTER_OFFERED) {
            throw new IllegalArgumentException("Solo se pueden rechazar transferencias en curso.");
        }
        transfer.setStatus(TransferStatus.REJECTED);
        transfer.setResolvedAt(LocalDateTime.now());
        return TransferResponse.fromEntity(transferRepository.save(transfer));
    }

    @Transactional
    public TransferResponse cancelTransfer(Long id, Long userId) {
        Transfer transfer = findOrThrow(id);
        if (transfer.getStatus() != TransferStatus.PENDING && transfer.getStatus() != TransferStatus.COUNTER_OFFERED) {
            throw new IllegalArgumentException("Solo se pueden cancelar transferencias en curso.");
        }
        transfer.setStatus(TransferStatus.CANCELLED);
        transfer.setResolvedAt(LocalDateTime.now());
        return TransferResponse.fromEntity(transferRepository.save(transfer));
    }

    private Transfer findOrThrow(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transferencia no encontrada con ID: " + id));
    }

    // --- AUCTION LOGIC ---

    private TransferResponse handleAuctionBid(Long playerId, Long bidderClubId, Integer bidRp) {
        List<Transfer> activeAuctions = transferRepository.findByPlayerIdAndStatus(playerId, TransferStatus.AUCTION);
        
        if (activeAuctions.isEmpty()) {
            // Start new auction
            clubClient.deductRp(bidderClubId, bidRp);
            
            Transfer auction = Transfer.builder()
                    .playerId(playerId)
                    .toClubId(bidderClubId)
                    .transferFeeRp(bidRp)
                    .status(TransferStatus.AUCTION)
                    .auctionEndTime(LocalDateTime.now().plusMinutes(1))
                    .build();
            auction = transferRepository.save(auction);
            
            scheduleAuctionResolution(auction.getId());
            return TransferResponse.fromEntity(auction);
        } else {
            // Bid on existing auction
            Transfer auction = activeAuctions.get(0);
            if (bidRp <= auction.getTransferFeeRp()) {
                throw new IllegalArgumentException("La puja debe ser mayor que la oferta actual (" + auction.getTransferFeeRp() + " RP).");
            }
            if (LocalDateTime.now().isAfter(auction.getAuctionEndTime())) {
                throw new IllegalArgumentException("La subasta ya ha terminado.");
            }
            
            // Refund previous bidder
            clubClient.addRp(auction.getToClubId(), auction.getTransferFeeRp());
            // Deduct from new bidder
            clubClient.deductRp(bidderClubId, bidRp);
            
            auction.setToClubId(bidderClubId);
            auction.setTransferFeeRp(bidRp);
            return TransferResponse.fromEntity(transferRepository.save(auction));
        }
    }

    @Async
    public void scheduleAuctionResolution(Long transferId) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(60000); // 1 minute
                resolveAuction(transferId);
            } catch (InterruptedException e) {
                log.error("Auction timer interrupted for transfer {}", transferId);
                Thread.currentThread().interrupt();
            }
        });
    }

    @Transactional
    public void resolveAuction(Long transferId) {
        Optional<Transfer> opt = transferRepository.findById(transferId);
        if (opt.isPresent()) {
            Transfer auction = opt.get();
            if (auction.getStatus() == TransferStatus.AUCTION) {
                auction.setStatus(TransferStatus.ACCEPTED);
                auction.setResolvedAt(LocalDateTime.now());
                transferRepository.save(auction);
                
                try {
                    clubClient.transferPlayer(auction.getPlayerId(), auction.getToClubId());
                    log.info("Auction {} resolved. Player {} goes to club {}.", transferId, auction.getPlayerId(), auction.getToClubId());
                } catch (Exception e) {
                    log.error("Failed to assign player {} to club {} after auction {}", auction.getPlayerId(), auction.getToClubId(), transferId, e);
                }
            }
        }
    }
}
