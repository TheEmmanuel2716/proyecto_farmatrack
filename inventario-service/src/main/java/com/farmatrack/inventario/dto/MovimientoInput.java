package com.farmatrack.inventario.dto;

import jakarta.validation.constraints.*;

public record MovimientoInput(@NotNull @Min(1) Integer cantidad, String observacion) {}
