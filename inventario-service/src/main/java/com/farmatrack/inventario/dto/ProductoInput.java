package com.farmatrack.inventario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductoInput(@NotBlank String nombre,
                            @NotBlank @JsonProperty("principio_activo") String principioActivo,
                            @NotNull @JsonProperty("proveedor_id") Long proveedorId) {}
