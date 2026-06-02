package com.tfg.esports.league;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Punto de entrada del League Service.
 *
 * <p>Gestiona las ligas, jornadas, partidos y clasificaciones.
 * Se registra en Eureka bajo el nombre "LEAGUE-SERVICE".</p>
 *
 * @author Pablo García Palacios
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class LeagueServiceApplication {

    /**
     * Método principal que arranca el League Service.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(LeagueServiceApplication.class, args);
    }
}
