package com.farmatrack.gateway.dto;

public record Producto(
        Long id,
        String nombre,
        String principio_activo,
        Long proveedor_id) {

    public String principioActivo() {
        return principio_activo;
    }

    public Long proveedorId() {
        return proveedor_id;
    }
}
