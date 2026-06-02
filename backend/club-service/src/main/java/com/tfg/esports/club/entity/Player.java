package com.tfg.esports.club.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un jugador profesional de League of Legends.
 *
 * <p>Un jugador puede pertenecer a un club o estar libre (agente libre).
 * El trigger {@code trg_check_role_limit} en la BD limita a 2 jugadores
 * por rol en cada club.</p>
 *
 * @author Pablo García Palacios
 */
@Entity
@Table(
    name = "players",
    indexes = {
        @Index(name = "idx_players_club",   columnList = "current_club_id"),
        @Index(name = "idx_players_role",   columnList = "lol_role"),
        @Index(name = "idx_players_rating", columnList = "overall_rating"),
        @Index(name = "idx_players_free",   columnList = "is_free_agent")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    /** Identificador único autoincremental */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de invocador único en el servidor */
    @Column(name = "summoner_name", unique = true, nullable = false, length = 100)
    private String summonerName;

    /** Nombre real del jugador */
    @Column(name = "real_name", length = 100)
    private String realName;

    /** Nacionalidad del jugador */
    @Column(length = 50)
    private String nationality;

    /** Edad del jugador (entre 16 y 50, validado en BD) */
    private Integer age;

    /** Rol principal del jugador en LoL */
    @Enumerated(EnumType.STRING)
    @Column(name = "lol_role", nullable = false, length = 20)
    private LolRole lolRole;

    /**
     * Valor de mercado actual del jugador en euros.
     * No puede ser negativo (constraint {@code chk_market_value} en BD).
     */
    @Column(name = "market_value", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal marketValue = new BigDecimal("50000.00");

    /**
     * Club al que pertenece el jugador actualmente.
     * {@code null} si es agente libre.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_club_id")
    private Club club;

    /**
     * Indica si el jugador está libre (sin club).
     * Se actualiza automáticamente al fichar o liberarle.
     */
    @Column(name = "is_free_agent", nullable = false)
    @Builder.Default
    private Boolean isFreeAgent = true;

    /**
     * Valoración general del jugador (1–99).
     * Validado con constraints en BD.
     */
    @Column(name = "overall_rating", nullable = false)
    @Builder.Default
    private Integer overallRating = 70;

    /** Fecha y hora de creación del registro */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
