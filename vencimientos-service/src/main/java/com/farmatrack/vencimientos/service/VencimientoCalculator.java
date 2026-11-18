package com.farmatrack.vencimientos.service;

import com.farmatrack.vencimientos.entity.EstadoVencimiento;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class VencimientoCalculator {
    public Resultado calcular(LocalDate fechaVencimiento, LocalDate hoy) {
        int dias = Math.toIntExact(ChronoUnit.DAYS.between(hoy, fechaVencimiento));
        EstadoVencimiento estado = dias < 0
                ? EstadoVencimiento.VENCIDO
                : dias <= 30 ? EstadoVencimiento.PROXIMO_A_VENCER : EstadoVencimiento.VIGENTE;
        return new Resultado(estado, dias);
    }

    public record Resultado(EstadoVencimiento estado, int diasParaVencer) {
    }
}
