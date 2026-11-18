package com.farmatrack.vencimientos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.farmatrack.vencimientos.entity.EstadoVencimiento;

import java.time.OffsetDateTime;

public record LoteEstadoResponse(Long id,
                                 @JsonProperty("lote_id") Long loteId,
                                 @JsonProperty("stock_actual") Integer stockActual,
                                 EstadoVencimiento estado,
                                 @JsonProperty("dias_para_vencer") Integer diasParaVencer,
                                 @JsonProperty("actualizado_en") OffsetDateTime actualizadoEn) {
}
