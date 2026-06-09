package com.tfg.esports.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Solicitud de creación de usuario por parte de un administrador.
 */
@Data
public class AdminUserCreateRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String username;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    private String role; // ROLE_ADMIN o ROLE_OWNER
}
