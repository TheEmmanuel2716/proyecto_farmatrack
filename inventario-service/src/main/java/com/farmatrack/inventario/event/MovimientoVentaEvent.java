package com.farmatrack.inventario.event;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.UUID;
public record MovimientoVentaEvent(@JsonProperty("evento_uuid") UUID eventoUuid,
                                    @JsonProperty("lote_id") Long loteId,
                                    Integer cantidad,
                                    @JsonProperty("stock_actual") Integer stockActual,
                                    @JsonProperty("fecha_hora") OffsetDateTime fechaHora,
                                    String tipo) {}
