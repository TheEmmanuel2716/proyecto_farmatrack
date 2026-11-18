package com.farmatrack.inventario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.*;

public record LoteResponse(Long id, @JsonProperty("numero_lote") String numeroLote,
                           @JsonProperty("fecha_vencimiento") LocalDate fechaVencimiento,
                           @JsonProperty("cantidad_inicial") Integer cantidadInicial,
                           @JsonProperty("producto_id") Long productoId,
                           @JsonProperty("stock_actual") Integer stockActual,
                           @JsonProperty("creado_en") OffsetDateTime creadoEn) {}
