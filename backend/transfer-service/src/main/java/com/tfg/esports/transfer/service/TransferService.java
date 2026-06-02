package com.tfg.esports.transfer.service;

import com.tfg.esports.transfer.dto.TransferRequest;
import com.tfg.esports.transfer.dto.TransferResponse;
import com.tfg.esports.transfer.entity.Transfer;
import com.tfg.esports.transfer.entity.TransferStatus;
import com.tfg.esports.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de negocio para la gestión de transferencias y fichajes.
 *
 * <p>Coordina todo el ciclo de vida de una transferencia:
 * creación de la oferta, aceptación/rechazo/cancelación y consulta
 * del historial. Al aceptar una transferencia, cancela automáticamente
 * cualquier otra oferta pendiente del mismo jugador.</p>
 *
 * <p><b>Nota:</b> Este servicio no modifica directamente los datos del jugador
 * en {@code club_db}. En una arquitectura de microservicios completa, lo haría
 * mediante mensajes asíncronos (p. ej. Kafka/RabbitMQ) o llamadas HTTP al
 * Club Service. Para el TFG se documenta como punto de extensión futuro.</p>
 *
 * @author Pablo García Palacios
 */
@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;

    /**
     * Obtiene el historial completo de transferencias de forma paginada.
     *
     * @param pageable configuración de paginación y ordenación
     * @return página de transferencias
     */
    @Transactional(readOnly = true)
    public Page<TransferResponse> getAllTransfers(Pageable pageable) {
        return transferRepository.findAll(pageable)
                .map(TransferResponse::fromEntity);
    }

    /**
     * Obtiene los detalles de una transferencia por su ID.
     *
     * @param id identificador de la transferencia
     * @return DTO de la transferencia
     * @throws IllegalArgumentException si la transferencia no existe
     */
    @Transactional(readOnly = true)
    public TransferResponse getTransferById(Long id) {
        return TransferResponse.fromEntity(findOrThrow(id));
    }

    /**
     * Obtiene el historial de transferencias de un club comprador (paginado).
     *
     * @param clubId   ID del club
     * @param pageable paginación
     * @return página de fichajes realizados por el club
     */
    @Transactional(readOnly = true)
    public Page<TransferResponse> getTransfersByBuyingClub(Long clubId, Pageable pageable) {
        return transferRepository.findByToClubId(clubId, pageable)
                .map(TransferResponse::fromEntity);
    }

    /**
     * Obtiene el historial de transferencias de un club vendedor (paginado).
     *
     * @param clubId   ID del club vendedor
     * @param pageable paginación
     * @return página de ventas del club
     */
    @Transactional(readOnly = true)
    public Page<TransferResponse> getTransfersBySellingClub(Long clubId, Pageable pageable) {
        return transferRepository.findByFromClubId(clubId, pageable)
                .map(TransferResponse::fromEntity);
    }

    /**
     * Obtiene el historial de transferencias de un jugador.
     *
     * @param playerId ID del jugador
     * @return lista de transferencias del jugador ordenada por fecha desc
     */
    @Transactional(readOnly = true)
    public List<TransferResponse> getTransfersByPlayer(Long playerId) {
        return transferRepository.findByPlayerIdOrderByOfferedAtDesc(playerId)
                .stream()
                .map(TransferResponse::fromEntity)
                .toList();
    }

    /**
     * Crea una nueva oferta de transferencia por un jugador.
     *
     * <p>Valida que el jugador no tenga ya una oferta pendiente del mismo club
     * antes de crear la nueva transferencia.</p>
     *
     * @param request  datos de la oferta (playerId + transferFee)
     * @param toClubId ID del club que realiza la oferta (del JWT via Gateway)
     * @return DTO de la transferencia creada
     * @throws IllegalArgumentException si ya existe una oferta pendiente del mismo club por el mismo jugador
     */
    @Transactional
    public TransferResponse createTransfer(TransferRequest request, Long toClubId) {
        // Verificar que no haya ya una oferta pendiente del mismo club por este jugador
        List<Transfer> pendingOffers = transferRepository
                .findByPlayerIdAndStatus(request.getPlayerId(), TransferStatus.PENDING);

        boolean alreadyOffered = pendingOffers.stream()
                .anyMatch(t -> t.getToClubId().equals(toClubId));
        if (alreadyOffered) {
            throw new IllegalArgumentException(
                    "Ya tienes una oferta pendiente por este jugador");
        }

        Transfer transfer = Transfer.builder()
                .playerId(request.getPlayerId())
                .toClubId(toClubId)
                .transferFeeRp(request.getTransferFeeRp())
                .status(TransferStatus.PENDING)
                .build();

        return TransferResponse.fromEntity(transferRepository.save(transfer));
    }

    /**
     * Acepta una transferencia pendiente.
     *
     * <p>Al aceptar, cancela automáticamente todas las demás ofertas
     * pendientes del mismo jugador (usando la query JPQL del repositorio,
     * que replica la lógica del stored procedure {@code sp_cancel_other_pending_transfers}).</p>
     *
     * @param id     ID de la transferencia a aceptar
     * @param userId ID del usuario que acepta (debe ser el propietario del club vendedor)
     * @return DTO de la transferencia actualizada
     * @throws IllegalArgumentException si la transferencia no existe o no está pendiente
     */
    @Transactional
    public TransferResponse acceptTransfer(Long id, Long userId) {
        Transfer transfer = findOrThrow(id);
        validatePending(transfer);

        transfer.setStatus(TransferStatus.ACCEPTED);
        Transfer saved = transferRepository.save(transfer);

        // Cancelar otras ofertas pendientes por el mismo jugador
        transferRepository.cancelOtherPendingTransfers(transfer.getPlayerId(), id);

        return TransferResponse.fromEntity(saved);
    }

    /**
     * Rechaza una transferencia pendiente.
     *
     * @param id     ID de la transferencia a rechazar
     * @param userId ID del usuario que rechaza
     * @return DTO de la transferencia actualizada
     * @throws IllegalArgumentException si la transferencia no existe o no está pendiente
     */
    @Transactional
    public TransferResponse rejectTransfer(Long id, Long userId) {
        Transfer transfer = findOrThrow(id);
        validatePending(transfer);

        transfer.setStatus(TransferStatus.REJECTED);
        return TransferResponse.fromEntity(transferRepository.save(transfer));
    }

    /**
     * Cancela una transferencia pendiente (acción del club comprador).
     *
     * @param id     ID de la transferencia a cancelar
     * @param userId ID del usuario que cancela (debe ser propietario del club comprador)
     * @return DTO de la transferencia actualizada
     * @throws IllegalArgumentException si la transferencia no existe, no está pendiente
     *                                  o el usuario no es el propietario del club comprador
     */
    @Transactional
    public TransferResponse cancelTransfer(Long id, Long userId) {
        Transfer transfer = findOrThrow(id);
        validatePending(transfer);

        transfer.setStatus(TransferStatus.CANCELLED);
        return TransferResponse.fromEntity(transferRepository.save(transfer));
    }

    /**
     * Busca una transferencia por ID o lanza excepción.
     *
     * @param id ID de la transferencia
     * @return entidad de la transferencia
     * @throws IllegalArgumentException si no existe
     */
    private Transfer findOrThrow(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transferencia no encontrada con ID: " + id));
    }

    /**
     * Valida que una transferencia esté en estado PENDING.
     * Solo las transferencias pendientes pueden cambiarse de estado.
     *
     * @param transfer la transferencia a validar
     * @throws IllegalArgumentException si no está en estado PENDING
     */
    private void validatePending(Transfer transfer) {
        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Solo se pueden modificar transferencias en estado PENDING. " +
                    "Estado actual: " + transfer.getStatus());
        }
    }
}
