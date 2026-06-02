package com.tfg.esports.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Punto de entrada del servidor Eureka.
 *
 * <p>Actúa como registro central de servicios: todos los microservicios
 * (auth, club, transfer, league) y el API Gateway se registran aquí
 * al arrancar, permitiendo el descubrimiento dinámico por nombre.</p>
 *
 * <p>Dashboard disponible en: http://localhost:8761</p>
 *
 * @author Pablo García Palacios
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    /**
     * Método principal que arranca el servidor Eureka.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
