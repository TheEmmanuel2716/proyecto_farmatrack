package com.farmatrack.inventario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MovimientoResponse(Long id, @JsonProperty("lote_id") Long loteId, String tipo,
                                 Integer cantidad, @JsonProperty("fecha_hora") OffsetDateTime fechaHora,
                                 @JsonProperty("evento_uuid") UUID eventoUuid) {}
