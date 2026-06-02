package com.tfg.esports.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para el Auth Service.
 *
 * <p>Intercepta las excepciones lanzadas en los controladores y las
 * convierte en respuestas HTTP con formato JSON consistente. Esto
 * evita que se expongan trazas de error al cliente.</p>
 *
 * @author Pablo García Palacios
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja los errores de validación de DTOs (Bean Validation).
     * Se lanza cuando un campo del request no cumple las restricciones
     * declaradas con {@code @NotBlank}, {@code @Email}, etc.
     *
     * @param ex excepción de validación
     * @return 400 Bad Request con el detalle de cada campo inválido
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Error de validación", fieldErrors);
    }

    /**
     * Maneja credenciales incorrectas en el login.
     *
     * @param ex excepción de credenciales inválidas
     * @return 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED,
                "Credenciales incorrectas", null);
    }

    /**
     * Maneja el intento de login de una cuenta deshabilitada.
     *
     * @param ex excepción de cuenta deshabilitada
     * @return 403 Forbidden
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabledAccount(DisabledException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN,
                "La cuenta está deshabilitada", null);
    }

    /**
     * Maneja errores de lógica de negocio (username/email duplicado,
     * refresh token inválido, etc.).
     *
     * @param ex excepción de argumento ilegal
     * @return 400 Bad Request con el mensaje de error
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /**
     * Manejador genérico para cualquier excepción no contemplada.
     *
     * @param ex excepción genérica
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor", null);
    }

    /**
     * Construye una respuesta de error con formato JSON consistente.
     *
     * @param status  código de estado HTTP
     * @param message mensaje de error principal
     * @param details detalles adicionales opcionales (p. ej. errores de campo)
     * @return ResponseEntity con el cuerpo de error estructurado
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String message, Object details) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        if (details != null) {
            body.put("details", details);
        }

        return ResponseEntity.status(status).body(body);
    }
}
