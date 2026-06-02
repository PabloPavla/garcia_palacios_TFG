package com.tfg.esports.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Entidad que representa un usuario del sistema.
 *
 * <p>Implementa {@link UserDetails} para integrarse con Spring Security.
 * Cada usuario tiene un rol único que determina sus permisos en la aplicación.</p>
 *
 * <p>La tabla {@code users} en {@code auth_db} incluye índices sobre
 * {@code email} y {@code username} para búsquedas rápidas.</p>
 *
 * @author Pablo García Palacios
 */
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_email",    columnList = "email"),
        @Index(name = "idx_users_username", columnList = "username")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    /** Identificador único autoincremental */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de usuario único (máx. 50 caracteres) */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /** Correo electrónico único del usuario */
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    /** Contraseña hasheada con BCrypt */
    @Column(nullable = false)
    private String password;

    /** Rol del usuario: ROLE_ADMIN o ROLE_OWNER */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** Indica si la cuenta está activa. Si es false, el login falla. */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Fecha y hora de creación de la cuenta (se asigna automáticamente) */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ──────────────────────────────────────────────────────────
    // Implementación de UserDetails (Spring Security)
    // ──────────────────────────────────────────────────────────

    /**
     * Devuelve la lista de autoridades (roles) del usuario.
     * Spring Security usa esto para las comprobaciones de acceso.
     *
     * @return colección con el único rol del usuario
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /** @return true – las cuentas no expiran en esta aplicación */
    @Override
    public boolean isAccountNonExpired() { return true; }

    /** @return true si la cuenta está activa (no bloqueada) */
    @Override
    public boolean isAccountNonLocked() { return active; }

    /** @return true – las credenciales no expiran en esta aplicación */
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    /** @return true si la cuenta está habilitada */
    @Override
    public boolean isEnabled() { return active; }
}
