package com.tfg.esports.auth.config;

import com.tfg.esports.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de Spring Security para el Auth Service.
 *
 * <p>El Auth Service usa una arquitectura <b>stateless</b>:
 * no mantiene sesiones HTTP en servidor. La autenticación se
 * delega en los tokens JWT gestionados por el {@link com.tfg.esports.auth.service.JwtService}.</p>
 *
 * <p>Los endpoints {@code /auth/register}, {@code /auth/login} y
 * {@code /auth/refresh} son públicos. El resto requiere autenticación.</p>
 *
 * @author Pablo García Palacios
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    private final GatewayHeaderFilter gatewayHeaderFilter;

    /**
     * Configura la cadena de filtros de seguridad HTTP.
     *
     * <ul>
     *   <li>Deshabilita CSRF (no necesario para APIs REST stateless)</li>
     *   <li>Configura sesión como STATELESS</li>
     *   <li>Permite sin autenticación las rutas públicas de auth</li>
     *   <li>Requiere autenticación para el resto de rutas</li>
     * </ul>
     *
     * @param http objeto de configuración de seguridad HTTP
     * @return la cadena de filtros configurada
     * @throws Exception si hay un error en la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Deshabilitar CSRF – no necesario en APIs REST con JWT
                .csrf(AbstractHttpConfigurer::disable)
                // No mantener sesiones en servidor (stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Configuración de autorización de rutas
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas (no requieren token)
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login",
                                "/auth/refresh",
                                "/actuator/**"
                        ).permitAll()
                        // El resto requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayHeaderFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider())
                .build();
    }

    /**
     * Proveedor de autenticación que carga usuarios desde la BD
     * y verifica contraseñas con BCrypt.
     *
     * @return el proveedor de autenticación configurado
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Servicio que carga el usuario por username desde la base de datos.
     * Usado internamente por Spring Security durante el proceso de autenticación.
     *
     * @return implementación de UserDetailsService
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username));
    }

    /**
     * Encoder de contraseñas BCrypt con coste por defecto (10 rondas).
     * Todas las contraseñas se almacenan hasheadas con BCrypt.
     *
     * @return instancia de BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Gestor de autenticación de Spring Security.
     * Se inyecta en {@link com.tfg.esports.auth.service.AuthService} para
     * validar las credenciales del usuario en el proceso de login.
     *
     * @param config configuración de autenticación de Spring
     * @return instancia del AuthenticationManager
     * @throws Exception si hay un error al obtener el manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
