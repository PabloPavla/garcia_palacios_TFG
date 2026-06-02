package com.tfg.esports.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Punto de entrada del Auth Service.
 *
 * <p>Microservicio responsable de:
 * <ul>
 *   <li>Registro de nuevos usuarios</li>
 *   <li>Login y generación de tokens JWT</li>
 *   <li>Renovación de tokens con refresh token</li>
 *   <li>Logout e invalidación de refresh tokens</li>
 * </ul>
 * </p>
 *
 * <p>Se registra en Eureka bajo el nombre "AUTH-SERVICE".</p>
 *
 * @author Pablo García Palacios
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    /**
     * Método principal que arranca el Auth Service.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
