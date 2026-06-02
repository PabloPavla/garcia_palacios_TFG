package com.tfg.esports.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO de solicitud de registro de un nuevo usuario.
 *
 * <p>Contiene y valida los datos necesarios para crear una cuenta:
 * nombre de usuario, correo electrónico y contraseña.</p>
 *
 * @author Pablo García Palacios
 */
@Data
public class RegisterRequest {

    /** Nombre de usuario (entre 3 y 50 caracteres, sin espacios) */
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String username;

    /** Correo electrónico válido */
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    /** Contraseña (mínimo 8 caracteres) */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;
}
