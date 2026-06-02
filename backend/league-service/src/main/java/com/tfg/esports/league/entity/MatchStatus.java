package com.tfg.esports.league.entity;

/**
 * Estado de un partido en la liga.
 *
 * <ul>
 *   <li>{@link #SCHEDULED} – Partido programado, aún no jugado</li>
 *   <li>{@link #COMPLETED} – Partido finalizado con resultado</li>
 *   <li>{@link #CANCELLED} – Partido cancelado</li>
 * </ul>
 *
 * @author Pablo García Palacios
 */
public enum MatchStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED
}
