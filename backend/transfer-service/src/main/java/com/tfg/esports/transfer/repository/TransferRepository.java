package com.tfg.esports.transfer.repository;

import com.tfg.esports.transfer.entity.Transfer;
import com.tfg.esports.transfer.entity.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Transfer}.
 *
 * <p>Proporciona consultas para gestionar el historial de transferencias,
 * filtrar por estado y obtener estadísticas por club.</p>
 *
 * @author Pablo García Palacios
 */
@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    /**
     * Obtiene todas las transferencias de un club comprador (paginadas).
     * Usado para mostrar el historial de fichajes de un club.
     *
     * @param toClubId ID del club comprador
     * @param pageable configuración de paginación
     * @return página de transferencias del club
     */
    Page<Transfer> findByToClubId(Long toClubId, Pageable pageable);

    /**
     * Obtiene todas las transferencias de un club vendedor (paginadas).
     * Usado para mostrar las ventas realizadas por un club.
     *
     * @param fromClubId ID del club vendedor
     * @param pageable   configuración de paginación
     * @return página de transferencias salientes del club
     */
    Page<Transfer> findByFromClubId(Long fromClubId, Pageable pageable);

    /**
     * Lista las transferencias pendientes de un jugador concreto.
     * Usado para verificar si un jugador ya tiene ofertas activas.
     *
     * @param playerId ID del jugador
     * @param status   estado a filtrar (normalmente PENDING)
     * @return lista de transferencias del jugador con ese estado
     */
    List<Transfer> findByPlayerIdAndStatus(Long playerId, TransferStatus status);

    /**
     * Lista todas las transferencias de un jugador ordenadas por fecha descendente.
     *
     * @param playerId ID del jugador
     * @return historial completo de transferencias del jugador
     */
    List<Transfer> findByPlayerIdOrderByOfferedAtDesc(Long playerId);

    /**
     * Cancela todas las transferencias pendientes de un jugador excepto una.
     * Se usa cuando se acepta una oferta y hay que rechazar las demás.
     * (Complementa al stored procedure {@code sp_cancel_other_pending_transfers} en BD.)
     *
     * @param playerId   ID del jugador
     * @param excludedId ID de la transferencia que NO debe cancelarse
     */
    @Modifying
    @Query("""
        UPDATE Transfer t
        SET t.status = 'CANCELLED'
        WHERE t.playerId = :playerId
          AND t.id != :excludedId
          AND t.status = 'PENDING'
        """)
    void cancelOtherPendingTransfers(Long playerId, Long excludedId);
}
