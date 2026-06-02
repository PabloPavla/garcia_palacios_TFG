package com.tfg.esports.transfer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa una transferencia o fichaje de jugador.
 *
 * <p>Registra el historial completo de movimientos de jugadores entre clubes.
 * El trigger {@code trg_transfer_set_resolved_at} en la BD fija automáticamente
 * la fecha de resolución cuando el estado cambia de PENDING a otro.</p>
 *
 * <p>Las referencias a {@code playerId}, {@code fromClubId} y {@code toClubId}
 * son FK lógicas hacia {@code club_db} (no FK reales por separación de microservicios).</p>
 *
 * @author Pablo García Palacios
 */
@Entity
@Table(
    name = "transfers",
    indexes = {
        @Index(name = "idx_transfers_player",    columnList = "player_id"),
        @Index(name = "idx_transfers_to_club",   columnList = "to_club_id"),
        @Index(name = "idx_transfers_from_club", columnList = "from_club_id"),
        @Index(name = "idx_transfers_status",    columnList = "status"),
        @Index(name = "idx_transfers_offered",   columnList = "offered_at")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {

    /** Identificador único autoincremental */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID del jugador objeto de la transferencia.
     * Referencia lógica a {@code club_db.players}.
     */
    @Column(name = "player_id", nullable = false)
    private Long playerId;

    /**
     * ID del club que cede al jugador.
     * {@code null} si el jugador era agente libre.
     */
    @Column(name = "from_club_id")
    private Long fromClubId;

    /**
     * ID del club que ficha al jugador (club comprador).
     * Nunca es {@code null}.
     */
    @Column(name = "to_club_id", nullable = false)
    private Long toClubId;

    /**
     * Cantidad ofrecida por el jugador en euros.
     * No puede ser negativa (constraint en BD).
     */
    @Column(name = "transfer_fee", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal transferFee = BigDecimal.ZERO;

    /** Estado actual de la transferencia */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransferStatus status = TransferStatus.PENDING;

    /** Fecha y hora en que se realizó la oferta */
    @CreationTimestamp
    @Column(name = "offered_at", nullable = false, updatable = false)
    private LocalDateTime offeredAt;

    /**
     * Fecha y hora en que se resolvió la transferencia (aceptada/rechazada/cancelada).
     * Se asigna automáticamente por el trigger {@code trg_transfer_set_resolved_at}.
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
