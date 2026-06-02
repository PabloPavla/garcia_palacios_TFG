package com.tfg.esports.club;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Punto de entrada del Club Service.
 *
 * <p>Gestiona los clubes de esports y sus plantillas de jugadores.
 * Se registra en Eureka bajo el nombre "CLUB-SERVICE".</p>
 *
 * @author Pablo García Palacios
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ClubServiceApplication {

    /**
     * Método principal que arranca el Club Service.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(ClubServiceApplication.class, args);
    }
}
