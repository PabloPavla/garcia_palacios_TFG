package com.tfg.esports.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO de solicitud de login.
 *
 * <p>El usuario proporciona su username y contraseña para autenticarse
 * y obtener un par de tokens JWT.</p>
 *
 * @author Pablo García Palacios
 */
@Data
public class LoginRequest {

    /** Nombre de usuario registrado */
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    /** Contraseña del usuario */
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
