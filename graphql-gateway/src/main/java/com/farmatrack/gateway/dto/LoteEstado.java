package com.farmatrack.gateway.dto;

public record LoteEstado(
        Long id,
        Long lote_id,
        Integer stock_actual,
        String estado,
        Integer dias_para_vencer,
        String actualizado_en) {

    public Long loteId() {
        return lote_id;
    }

    public Integer stockActual() {
        return stock_actual;
    }

    public Integer diasParaVencer() {
        return dias_para_vencer;
    }

    public String actualizadoEn() {
        return actualizado_en;
    }
}
