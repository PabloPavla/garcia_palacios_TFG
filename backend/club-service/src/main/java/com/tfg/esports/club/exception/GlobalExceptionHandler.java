package com.tfg.esports.club.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para el Club Service.
 *
 * <p>Convierte las excepciones en respuestas HTTP con formato JSON
 * consistente para todos los endpoints del servicio.</p>
 *
 * @author Pablo García Palacios
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación de DTOs (Bean Validation).
     *
     * @param ex excepción de validación
     * @return 400 Bad Request con detalle por campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field   = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
        });
        return buildError(HttpStatus.BAD_REQUEST, "Error de validación", fieldErrors);
    }

    /**
     * Maneja errores de lógica de negocio (nombre duplicado, club no encontrado, etc.).
     *
     * @param ex excepción de argumento ilegal
     * @return 400 Bad Request con el mensaje de error
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /**
     * Maneja intentos de acceso no autorizado (propietario distinto).
     *
     * @param ex excepción de seguridad
     * @return 403 Forbidden
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurity(SecurityException ex) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    /**
     * Manejador genérico para excepciones no contempladas.
     *
     * @param ex excepción genérica
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", null);
    }

    /**
     * Construye una respuesta de error con formato JSON consistente.
     *
     * @param status  código HTTP
     * @param message mensaje principal
     * @param details detalles adicionales opcionales
     * @return ResponseEntity con el cuerpo de error
     */
    private ResponseEntity<Map<String, Object>> buildError(
            HttpStatus status, String message, Object details) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    status.value());
        body.put("error",     status.getReasonPhrase());
        body.put("message",   message);
        if (details != null) body.put("details", details);
        return ResponseEntity.status(status).body(body);
    }
}
