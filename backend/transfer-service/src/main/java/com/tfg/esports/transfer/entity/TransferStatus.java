package com.tfg.esports.transfer.entity;

/**
 * Estado de una transferencia/fichaje entre clubes.
 *
 * <ul>
 *   <li>{@link #PENDING}   – Oferta enviada, esperando respuesta del club vendedor</li>
 *   <li>{@link #ACCEPTED}  – Transferencia aceptada y completada</li>
 *   <li>{@link #REJECTED}  – Oferta rechazada por el club vendedor o el jugador</li>
 *   <li>{@link #CANCELLED} – Cancelada por el club comprador antes de ser resuelta</li>
 * </ul>
 *
 * @author Pablo García Palacios
 */
public enum TransferStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED
}
