package com.farmatrack.vencimientos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.farmatrack.vencimientos.entity.TipoAlerta;

import java.time.OffsetDateTime;

public record AlertaResponse(Long id,
                             @JsonProperty("lote_id") Long loteId,
                             @JsonProperty("tipo_alerta") TipoAlerta tipoAlerta,
                             @JsonProperty("dias_restantes") Integer diasRestantes,
                             @JsonProperty("generada_en") OffsetDateTime generadaEn,
                             boolean atendida) {
}
