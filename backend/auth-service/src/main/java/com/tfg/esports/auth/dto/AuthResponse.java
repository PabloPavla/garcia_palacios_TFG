package com.tfg.esports.auth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO de respuesta de autenticación.
 *
 * <p>Se devuelve tras un registro o login exitoso. Contiene
 * el access token JWT (de corta duración) y el refresh token
 * (de larga duración), junto con los datos básicos del usuario.</p>
 *
 * @author Pablo García Palacios
 */
@Data
@Builder
public class AuthResponse {

    /** Token JWT de acceso (expira en 1 hora por defecto) */
    private String accessToken;

    /** Refresh token para renovar el access token (expira en 7 días) */
    private String refreshToken;

    /** ID del usuario en base de datos */
    private Long userId;

    /** Nombre de usuario */
    private String username;

    /** Correo electrónico del usuario */
    private String email;

    /** Rol del usuario: "ROLE_ADMIN" o "ROLE_OWNER" */
    private String role;

    /** URL de la foto de perfil */
    private String profilePictureUrl;
}
