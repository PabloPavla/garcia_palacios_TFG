package com.tfg.esports.transfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Punto de entrada del Transfer Service.
 *
 * <p>Gestiona las transferencias y fichajes de jugadores entre clubes.
 * Se registra en Eureka bajo el nombre "TRANSFER-SERVICE".</p>
 *
 * @author Pablo García Palacios
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
public class TransferServiceApplication {

    /**
     * Método principal que arranca el Transfer Service.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(TransferServiceApplication.class, args);
    }
}
