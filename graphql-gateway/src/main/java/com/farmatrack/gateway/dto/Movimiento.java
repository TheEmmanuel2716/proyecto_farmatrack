package com.farmatrack.gateway.dto;

public record Movimiento(
        Long id,
        Long lote_id,
        String tipo,
        Integer cantidad,
        String fecha_hora,
        String evento_uuid) {

    public Long loteId() {
        return lote_id;
    }

    public String fechaHora() {
        return fecha_hora;
    }

    public String eventoUuid() {
        return evento_uuid;
    }
}
