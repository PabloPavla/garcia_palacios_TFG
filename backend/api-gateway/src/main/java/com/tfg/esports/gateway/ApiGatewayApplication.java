package com.tfg.esports.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Punto de entrada del API Gateway.
 *
 * <p>Actúa como puerta de entrada única para todos los clientes.
 * Se registra en Eureka como cliente para poder enrutar peticiones
 * a los microservicios usando balanceo de carga (lb://nombre-servicio).
 * Valida tokens JWT antes de reenviar las peticiones.</p>
 *
 * @author Pablo García Palacios
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    /**
     * Método principal que arranca el API Gateway.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
