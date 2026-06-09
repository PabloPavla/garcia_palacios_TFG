package com.tfg.esports.auth.controller;

import com.tfg.esports.auth.dto.AuthResponse;
import com.tfg.esports.auth.dto.LoginRequest;
import com.tfg.esports.auth.dto.RefreshRequest;
import com.tfg.esports.auth.dto.RegisterRequest;
import com.tfg.esports.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import java.security.Principal;
import java.util.Map;

/**
 * Controlador REST del Auth Service.
 *
 * <p>Expone los endpoints de autenticación bajo la ruta base {@code /auth}.
 * Todos los endpoints son accesibles públicamente excepto {@code /auth/logout}
 * y {@code /auth/me}, que requieren un token JWT válido.</p>
 *
 * <p>El API Gateway enruta las peticiones {@code /auth/**} a este servicio.</p>
 *
 * @author Pablo García Palacios
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <p>Si el username o email ya están en uso, devuelve 409 Conflict.
     * En caso de éxito, devuelve 201 Created con el par de tokens JWT.</p>
     *
     * @param request datos de registro (username, email, password)
     * @return 201 con {@link AuthResponse} si el registro es exitoso
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Autentica a un usuario existente y devuelve un par de tokens JWT.
     *
     * <p>Si las credenciales son incorrectas, Spring Security lanza una
     * excepción que el {@link com.tfg.esports.auth.exception.GlobalExceptionHandler}
     * convierte en 401 Unauthorized.</p>
     *
     * @param request credenciales (username, password)
     * @return 200 con {@link AuthResponse} si las credenciales son correctas
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Renueva el access token usando un refresh token válido.
     *
     * <p>Implementa rotación de refresh tokens: el token antiguo se invalida
     * y se emite un nuevo par de tokens.</p>
     *
     * @param request objeto con el refresh token a renovar
     * @return 200 con el nuevo par de tokens si el refresh token es válido
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Cierra la sesión del usuario autenticado.
     *
     * <p>Elimina todos los refresh tokens del usuario de la base de datos,
     * invalidando así cualquier sesión activa.</p>
     *
     * @param principal información del usuario autenticado (inyectada por Spring Security)
     * @return 200 con mensaje de confirmación
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(Principal principal) {
        authService.logout(principal.getName());
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }

    /**
     * Devuelve los datos del usuario autenticado actualmente.
     *
     * <p>Útil para que el frontend recupere el perfil del usuario
     * usando solo el token JWT sin necesidad de almacenar datos en local.</p>
     *
     * @param principal información del usuario autenticado
     * @return 200 con el username del usuario
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(Principal principal) {
        return ResponseEntity.ok(Map.of("username", principal.getName()));
    }

    /**
     * Actualiza el perfil del usuario autenticado.
     */
    @PutMapping("/profile")
    public ResponseEntity<AuthResponse> updateProfile(
            @Valid @RequestBody com.tfg.esports.auth.dto.ProfileUpdateRequest request,
            Principal principal) {
        AuthResponse response = authService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/by-username/{username}")
    public ResponseEntity<java.util.Map<String, Object>> getUserByUsername(@PathVariable String username) {
        return authService.findByUsername(username)
                .map(u -> ResponseEntity.ok(java.util.Map.of(
                        "id", (Object) u.getId(),
                        "username", (Object) u.getUsername(),
                        "profilePictureUrl", u.getProfilePictureUrl() != null ? u.getProfilePictureUrl() : ""
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<java.util.Map<String, String>> deleteUser(@PathVariable Long id, Principal principal) {
        authService.deleteUser(id, principal.getName());
        return ResponseEntity.ok(java.util.Map.of("message", "Usuario eliminado correctamente"));
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> createUserByAdmin(
            @Valid @RequestBody com.tfg.esports.auth.dto.AdminUserCreateRequest request) {
        com.tfg.esports.auth.entity.User user = authService.createUserByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "active", user.isActive()
        ));
    }
}
