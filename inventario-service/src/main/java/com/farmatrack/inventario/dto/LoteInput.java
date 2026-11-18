package com.farmatrack.inventario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record LoteInput(@NotBlank @JsonProperty("numero_lote") String numeroLote,
                        @NotNull @JsonProperty("fecha_vencimiento") LocalDate fechaVencimiento,
                        @NotNull @Min(1) @JsonProperty("cantidad_inicial") Integer cantidadInicial) {}
