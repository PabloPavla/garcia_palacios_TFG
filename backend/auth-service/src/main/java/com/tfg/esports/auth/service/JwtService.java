package com.tfg.esports.auth.service;

import com.tfg.esports.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Servicio responsable de generar y validar tokens JWT.
 *
 * <p>Usa el algoritmo HMAC-SHA256 con una clave secreta configurada
 * via variable de entorno. El token incluye el ID, username y rol
 * del usuario para que los servicios downstream puedan identificarlo
 * sin consultar la base de datos.</p>
 *
 * @author Pablo García Palacios
 */
@Service
public class JwtService {

    /** Clave secreta Base64 para firmar los tokens (mín. 256 bits) */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /** Duración del access token en milisegundos (por defecto 1 hora) */
    @Value("${app.jwt.expiration:3600000}")
    private long jwtExpiration;

    /**
     * Genera un access token JWT para el usuario dado.
     *
     * <p>El token incluye:
     * <ul>
     *   <li>{@code sub} – username del usuario</li>
     *   <li>{@code userId} – ID del usuario en base de datos</li>
     *   <li>{@code role} – rol del usuario (ROLE_ADMIN / ROLE_OWNER)</li>
     *   <li>{@code iat} – fecha de emisión</li>
     *   <li>{@code exp} – fecha de expiración</li>
     * </ul>
     * </p>
     *
     * @param user el usuario para el que se genera el token
     * @return token JWT firmado como String
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", String.valueOf(user.getId()))
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrae el nombre de usuario (claim "sub") del token JWT.
     *
     * @param token el token JWT
     * @return nombre de usuario
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Valida que el token JWT tenga firma correcta y no haya expirado.
     *
     * @param token el token JWT a validar
     * @return true si es válido, false si está expirado o malformado
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
     * Parsea el token y devuelve todos los claims del payload.
     *
     * @param token el token JWT
     * @return Claims con todos los datos
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
     * Construye la clave HMAC-SHA a partir del secreto Base64.
     *
     * @return SecretKey lista para firmar o verificar tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
