package com.farmatrack.inventario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductoResponse(Long id, String nombre,
                               @JsonProperty("principio_activo") String principioActivo,
                               @JsonProperty("proveedor_id") Long proveedorId) {}
