package com.tfg.esports.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO de solicitud de renovación de token.
 *
 * <p>El cliente envía el refresh token obtenido durante el login
 * para obtener un nuevo access token JWT sin necesidad de
 * introducir de nuevo las credenciales.</p>
 *
 * @author Pablo García Palacios
 */
@Data
public class RefreshRequest {

    /** Valor del refresh token (UUID) almacenado en el cliente */
    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}
