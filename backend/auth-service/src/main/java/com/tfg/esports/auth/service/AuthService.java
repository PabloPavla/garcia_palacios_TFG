package com.tfg.esports.auth.service;

import com.tfg.esports.auth.dto.AuthResponse;
import com.tfg.esports.auth.dto.LoginRequest;
import com.tfg.esports.auth.dto.RegisterRequest;
import com.tfg.esports.auth.entity.RefreshToken;
import com.tfg.esports.auth.entity.Role;
import com.tfg.esports.auth.entity.User;
import com.tfg.esports.auth.repository.RefreshTokenRepository;
import com.tfg.esports.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio de negocio del Auth Service.
 *
 * <p>Contiene toda la lógica de autenticación: registro, login,
 * renovación de tokens y logout. Coordina los repositorios,
 * el {@link JwtService} y el {@link PasswordEncoder} de Spring Security.</p>
 *
 * @author Pablo García Palacios
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository         userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService             jwtService;
    private final PasswordEncoder        passwordEncoder;
    private final AuthenticationManager  authenticationManager;

    /** Duración del refresh token en milisegundos (por defecto 7 días) */
    @Value("${app.jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <p>Pasos:
     * <ol>
     *   <li>Verifica que el username y email no estén ya en uso</li>
     *   <li>Hashea la contraseña con BCrypt</li>
     *   <li>Persiste el usuario con rol ROLE_OWNER por defecto</li>
     *   <li>Genera y devuelve el par de tokens (access + refresh)</li>
     * </ol>
     * </p>
     *
     * @param request datos de registro (username, email, password)
     * @return {@link AuthResponse} con el par de tokens JWT
     * @throws IllegalArgumentException si el username o email ya existen
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Verificar que no exista ya un usuario con ese username o email
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }

        // Crear y persistir el usuario
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_OWNER)
                .active(true)
                .build();

        userRepository.save(user);

        // Generar tokens y devolver respuesta
        return generateTokenPair(user);
    }

    /**
     * Autentica a un usuario y devuelve un par de tokens JWT.
     *
     * <p>Delega la verificación de credenciales en el {@link AuthenticationManager}
     * de Spring Security, que internamente usa BCrypt para comparar contraseñas.</p>
     *
     * @param request credenciales de login (username, password)
     * @return {@link AuthResponse} con el par de tokens JWT
     * @throws org.springframework.security.core.AuthenticationException si las credenciales son incorrectas
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Spring Security valida las credenciales y lanza excepción si son incorrectas
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Eliminar refresh tokens anteriores del usuario (una sesión activa por usuario)
        refreshTokenRepository.deleteAllByUser(user);

        return generateTokenPair(user);
    }

    /**
     * Renueva el access token usando un refresh token válido.
     *
     * <p>Si el refresh token es válido y no ha expirado, genera
     * un nuevo par de tokens y elimina el refresh token antiguo.</p>
     *
     * @param refreshTokenValue valor del refresh token
     * @return {@link AuthResponse} con el nuevo par de tokens
     * @throws IllegalArgumentException si el refresh token no existe o ha expirado
     */
    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        // Buscar el refresh token en la BD
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido"));

        // Verificar que no haya expirado
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token expirado, inicia sesión de nuevo");
        }

        User user = refreshToken.getUser();

        // Eliminar el refresh token usado e invalidarlo (rotación de tokens)
        refreshTokenRepository.delete(refreshToken);

        return generateTokenPair(user);
    }

    /**
     * Cierra la sesión del usuario eliminando sus refresh tokens de la BD.
     *
     * @param username nombre del usuario que hace logout
     */
    @Transactional
    public void logout(String username) {
        userRepository.findByUsername(username).ifPresent(
                user -> refreshTokenRepository.deleteAllByUser(user)
        );
    }

    /**
     * Genera un par de tokens (access token JWT + refresh token UUID)
     * y persiste el refresh token en la base de datos.
     *
     * @param user el usuario para el que se generan los tokens
     * @return {@link AuthResponse} con ambos tokens y los datos del usuario
     */
    private AuthResponse generateTokenPair(User user) {
        // Generar access token JWT
        String accessToken = jwtService.generateToken(user);

        // Generar refresh token aleatorio (UUID) y persistirlo
        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
