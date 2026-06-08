package com.tfg.esports.club.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un club de esports de League of Legends.
 *
 * <p>Cada club pertenece a un usuario propietario (referenciado por
 * {@code ownerId}, FK lógica hacia {@code auth_db.users}).
 * Tiene un presupuesto para realizar fichajes y una plantilla de jugadores.</p>
 *
 * @author Pablo García Palacios
 */
@Entity
@Table(
    name = "clubs",
    indexes = {
        @Index(name = "idx_clubs_owner",    columnList = "owner_id"),
        @Index(name = "idx_clubs_division", columnList = "division")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Club {

    /** Identificador único autoincremental */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre completo del club (debe ser único) */
    @Column(unique = true, nullable = false, length = 100)
    private String name;

    /** Acrónimo del club (p. ej. "T1", "FNC") de máximo 5 caracteres */
    @Column(nullable = false, length = 5)
    private String acronym;

    /** URL del logo del club (almacenado en CDN o como ruta relativa) */
    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    /**
     * Presupuesto actual del club en Riot Points.
     * No puede ser negativo (constraint {@code chk_riot_points} en BD).
     */
    @Column(name = "riot_points", nullable = false)
    @Builder.Default
    private Integer riotPoints = 2000;

    /**
     * ID del propietario del club (referencia lógica a {@code auth_db.users}).
     * No es FK real porque cada microservicio tiene su propia BD.
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /** División competitiva actual del club */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Division division = Division.BRONZE;

    /** Fecha y hora de creación del club */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Plantilla de jugadores del club.
     * Un club puede tener máximo 10 jugadores (2 por cada uno de los 5 roles).
     * El trigger {@code trg_check_role_limit} en BD garantiza este límite.
     */
    @OneToMany(mappedBy = "club", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Player> players = new ArrayList<>();
}
