package com.farmatrack.inventario.dto;

import java.time.OffsetDateTime;

public record ErrorResponse(String codigo, String mensaje, OffsetDateTime timestamp) {}
