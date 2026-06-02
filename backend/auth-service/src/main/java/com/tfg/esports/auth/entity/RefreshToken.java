package com.tfg.esports.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un refresh token almacenado en base de datos.
 *
 * <p>Cuando el access token (JWT) expira, el cliente puede usar el
 * refresh token para obtener un nuevo par de tokens sin necesidad
 * de volver a hacer login.</p>
 *
 * <p>Al hacer logout, el refresh token se elimina de la BD, lo que
 * invalida la sesión del usuario de forma segura.</p>
 *
 * @author Pablo García Palacios
 */
@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
        @Index(name = "idx_refresh_token", columnList = "token")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    /** Identificador único autoincremental */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relación con el usuario propietario del token.
     * Si el usuario se elimina, el refresh token se elimina en cascada (ON DELETE CASCADE).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Valor UUID único del refresh token */
    @Column(unique = true, nullable = false, length = 255)
    private String token;

    /** Fecha y hora de expiración del refresh token */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Fecha y hora de creación del refresh token */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Comprueba si el refresh token ha expirado.
     *
     * @return true si el token ya ha expirado
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
