package com.farmatrack.gateway.dto;

public record Lote(
        Long id,
        String numero_lote,
        String fecha_vencimiento,
        Integer cantidad_inicial,
        Long producto_id,
        Integer stock_actual,
        String creado_en) {

    public String numeroLote() {
        return numero_lote;
    }

    public String fechaVencimiento() {
        return fecha_vencimiento;
    }

    public Integer cantidadInicial() {
        return cantidad_inicial;
    }

    public Long productoId() {
        return producto_id;
    }

    public Integer stockActual() {
        return stock_actual;
    }

    public String creadoEn() {
        return creado_en;
    }
}
