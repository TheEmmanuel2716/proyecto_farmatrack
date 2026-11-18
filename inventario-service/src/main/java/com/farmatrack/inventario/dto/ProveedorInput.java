package com.farmatrack.inventario.dto;

import jakarta.validation.constraints.NotBlank;

public record ProveedorInput(@NotBlank String nombre, @NotBlank String nit, String contacto) {}
