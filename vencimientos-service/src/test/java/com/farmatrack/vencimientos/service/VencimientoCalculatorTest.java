package com.farmatrack.vencimientos.service;

import com.farmatrack.vencimientos.entity.EstadoVencimiento;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VencimientoCalculatorTest {
    private final VencimientoCalculator calculator = new VencimientoCalculator();
    private final LocalDate today = LocalDate.of(2026, 9, 18);

    @Test
    void calculaEstadoYDiasRestantes() {
        assertEquals(new VencimientoCalculator.Resultado(EstadoVencimiento.VIGENTE, 31),
                calculator.calcular(today.plusDays(31), today));
        assertEquals(new VencimientoCalculator.Resultado(EstadoVencimiento.PROXIMO_A_VENCER, 30),
                calculator.calcular(today.plusDays(30), today));
        assertEquals(new VencimientoCalculator.Resultado(EstadoVencimiento.VENCIDO, -1),
                calculator.calcular(today.minusDays(1), today));
    }
}
