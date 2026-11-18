package com.farmatrack.inventario.event;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.UUID;
public record LoteIngresadoEvent(@JsonProperty("evento_uuid") UUID eventoUuid,
                                  @JsonProperty("lote_id") Long loteId,
                                  @JsonProperty("producto_id") Long productoId,
                                  @JsonProperty("fecha_vencimiento") LocalDate fechaVencimiento,
                                  @JsonProperty("stock_actual") Integer stockActual,
                                  String tipo) {}
