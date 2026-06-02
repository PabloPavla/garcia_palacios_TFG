package com.tfg.esports.gateway.filter;

import com.tfg.esports.gateway.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Filtro global de autenticación JWT para el API Gateway.
 *
 * <p>Se ejecuta en TODAS las peticiones entrantes. Si el endpoint no es
 * público, verifica el token JWT de la cabecera Authorization. Si es
 * válido, extrae el usuario y lo añade como cabeceras HTTP para que
 * los microservicios destino puedan identificarlo sin necesidad de
 * validar el JWT de nuevo.</p>
 *
 * <p>Cabeceras añadidas a las peticiones downstream:
 * <ul>
 *   <li>{@code X-Auth-User-Id}   – ID del usuario</li>
 *   <li>{@code X-Auth-Username}  – nombre de usuario</li>
 *   <li>{@code X-Auth-Role}      – rol del usuario (ROLE_ADMIN / ROLE_OWNER)</li>
 * </ul>
 * </p>
 *
 * @author Pablo García Palacios
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    /** Prefijo estándar de la cabecera de autenticación Bearer */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Rutas públicas que NO requieren token JWT.
     * Cualquier petición cuya ruta empiece por alguna de estas cadenas
     * pasa sin validación.
     */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/register",
            "/auth/login",
            "/auth/refresh",
            "/actuator"
    );

    @Autowired
    private JwtService jwtService;

    /**
     * Lógica principal del filtro. Se ejecuta de forma reactiva (WebFlux).
     *
     * @param exchange el intercambio HTTP (petición + respuesta)
     * @param chain    la cadena de filtros a continuar si la validación es correcta
     * @return un {@link Mono} que representa el procesamiento asíncrono
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Permitir rutas públicas sin validación de JWT
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Obtener la cabecera Authorization
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return respondWithUnauthorized(exchange, "Falta la cabecera Authorization o el formato es incorrecto");
        }

        // Extraer el token (sin el prefijo "Bearer ")
        String token = authHeader.substring(BEARER_PREFIX.length());

        // Validar el token JWT
        if (!jwtService.validateToken(token)) {
            return respondWithUnauthorized(exchange, "Token JWT inválido o expirado");
        }

        // Extraer información del usuario del token y pasarla como cabeceras
        String username = jwtService.extractUsername(token);
        String role     = jwtService.extractRole(token);
        String userId   = jwtService.extractUserId(token);

        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Auth-User-Id",  userId)
                .header("X-Auth-Username", username)
                .header("X-Auth-Role",     role)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    /**
     * Comprueba si la ruta solicitada es pública (no requiere JWT).
     *
     * @param path ruta de la petición HTTP
     * @return true si la ruta es pública
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Devuelve una respuesta 401 Unauthorized y termina la cadena de filtros.
     *
     * @param exchange el intercambio HTTP actual
     * @param message  mensaje de error (se registra en log, no se devuelve al cliente por seguridad)
     * @return Mono vacío que completa la respuesta
     */
    private Mono<Void> respondWithUnauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    /**
     * Prioridad del filtro. Valor negativo = alta prioridad (se ejecuta antes que otros filtros).
     *
     * @return orden de ejecución del filtro
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
