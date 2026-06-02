package com.tfg.esports.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

/**
 * Servicio de validación de tokens JWT en el API Gateway.
 *
 * <p>El Gateway no genera tokens (eso lo hace el Auth Service),
 * solo los valida para decidir si la petición puede seguir adelante
 * y extrae los datos del usuario para pasarlos como cabeceras HTTP
 * a los microservicios de destino.</p>
 *
 * @author Pablo García Palacios
 */
@Service
public class JwtService {

    /** Clave secreta Base64 inyectada desde la variable de entorno JWT_SECRET */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /**
     * Valida si el token JWT es correcto: firma válida y no expirado.
     *
     * @param token el token JWT a validar (sin el prefijo "Bearer ")
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extrae el nombre de usuario (subject) del token JWT.
     *
     * @param token el token JWT
     * @return nombre de usuario almacenado en el claim "sub"
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extrae el rol del usuario del token JWT.
     *
     * @param token el token JWT
     * @return rol del usuario (p. ej. "ROLE_OWNER", "ROLE_ADMIN")
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Extrae el ID del usuario del token JWT.
     *
     * @param token el token JWT
     * @return ID del usuario como String
     */
    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    /**
     * Parsea el token y extrae todos los claims (payload).
     *
     * @param token el token JWT
     * @return Claims con todos los datos del payload
     * @throws JwtException si el token es inválido o está expirado
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Construye la clave de firma HMAC-SHA a partir del secreto Base64.
     *
     * @return SecretKey para verificar la firma del token
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
