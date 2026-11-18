package com.farmatrack.vencimientos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VencimientosServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VencimientosServiceApplication.class, args);
    }
}
