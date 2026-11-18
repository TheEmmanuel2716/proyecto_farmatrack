package com.farmatrack.gateway.dto;

public record Alerta(
        Long id,
        Long lote_id,
        String tipo_alerta,
        Integer dias_restantes,
        String generada_en,
        boolean atendida) {

    public Long loteId() {
        return lote_id;
    }

    public String tipoAlerta() {
        return tipo_alerta;
    }

    public Integer diasRestantes() {
        return dias_restantes;
    }

    public String generadaEn() {
        return generada_en;
    }
}
