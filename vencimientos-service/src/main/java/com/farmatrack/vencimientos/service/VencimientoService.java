package com.farmatrack.vencimientos.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.farmatrack.vencimientos.dto.*;
import com.farmatrack.vencimientos.entity.*;
import com.farmatrack.vencimientos.exception.RecursoNoEncontradoException;
import com.farmatrack.vencimientos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VencimientoService {
    private static final int[] UMBRALES = {30, 15, 7};
    private final LoteEstadoRepository loteEstadoRepository;
    private final AlertaRepository alertaRepository;
    private final EventoProcesadoRepository eventoProcesadoRepository;
    private final VencimientoCalculator calculator;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Transactional(readOnly = true)
    public LoteEstadoResponse findEstado(Long loteId) {
        return loteEstadoRepository.findByLoteId(loteId)
                .map(this::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "LOTE_NO_SINCRONIZADO",
                        "No se ha sincronizado ningún evento para el lote: " + loteId));
    }

    @Transactional(readOnly = true)
    public Page<AlertaResponse> findAlerts(Boolean atendida, Pageable pageable) {
        Page<Alerta> page = atendida == null
                ? alertaRepository.findAll(pageable)
                : alertaRepository.findByAtendida(atendida, pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AlertaResponse findAlert(Long alertaId) {
        return toResponse(alertaRepository.findById(alertaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alerta no encontrada: " + alertaId)));
    }

    @Transactional
    public AlertaResponse markAlert(Long alertaId, boolean atendida) {
        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alerta no encontrada: " + alertaId));
        alerta.setAtendida(atendida);
        return toResponse(alertaRepository.save(alerta));
    }

    /**
     * Consumes the wire-compatible JSON emitted by inventario-service. Type
     * headers are deliberately not required because the producer disables them.
     */
    @Transactional
    public void processEvent(String payload) {
        try {
            JsonNode event = objectMapper.readTree(payload);
            String type = text(event, "tipo");
            UUID eventUuid = UUID.fromString(requiredText(event, "evento_uuid"));
            if (!Set.of("LOTE_INGRESADO", "MOVIMIENTO_VENTA", "MOVIMIENTO_DEVOLUCION").contains(type)) {
                return;
            }
            if (eventoProcesadoRepository.existsById(eventUuid)) {
                return;
            }
            Long loteId = requiredLong(event, "lote_id");
            Integer stock = requiredInt(event, "stock_actual");
            if ("LOTE_INGRESADO".equals(type)) {
                LocalDate expiry = LocalDate.parse(requiredText(event, "fecha_vencimiento"));
                upsert(loteId, expiry, stock);
            } else {
                loteEstadoRepository.findByLoteId(loteId)
                        .ifPresent(state -> {
                            state.setStockActual(stock);
                            recalculateAndSave(state);
                        });
            }
            eventoProcesadoRepository.save(new EventoProcesado(eventUuid, OffsetDateTime.now(clock)));
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new IllegalArgumentException("Evento de inventario inválido", e);
        }
    }

    @Transactional
    public void processScheduledAlerts() {
        loteEstadoRepository.findAll().forEach(this::recalculateAndSave);
    }

    private void upsert(Long loteId, LocalDate expiry, Integer stock) {
        LoteEstado state = loteEstadoRepository.findByLoteId(loteId)
                .orElseGet(() -> LoteEstado.builder().loteId(loteId).build());
        state.setFechaVencimiento(expiry);
        state.setStockActual(stock);
        recalculateAndSave(state);
    }

    private void recalculateAndSave(LoteEstado state) {
        VencimientoCalculator.Resultado result =
                calculator.calcular(state.getFechaVencimiento(), LocalDate.now(clock));
        state.setEstado(result.estado());
        state.setDiasParaVencer(result.diasParaVencer());
        state.setActualizadoEn(OffsetDateTime.now(clock));
        LoteEstado saved = loteEstadoRepository.save(state);
        generateAlerts(saved);
    }

    private void generateAlerts(LoteEstado state) {
        int days = state.getDiasParaVencer();
        if (days < 0) {
            createAlert(state, TipoAlerta.VENCIDO, days);
        }
        for (int threshold : UMBRALES) {
            if (days >= 0 && days <= threshold) {
                TipoAlerta type = switch (threshold) {
                    case 30 -> TipoAlerta.PROXIMO_A_VENCER_30;
                    case 15 -> TipoAlerta.PROXIMO_A_VENCER_15;
                    default -> TipoAlerta.PROXIMO_A_VENCER_7;
                };
                createAlert(state, type, days);
            }
        }
    }

    private void createAlert(LoteEstado state, TipoAlerta type, int days) {
        if (alertaRepository.findByLoteIdAndTipoAlerta(state.getLoteId(), type).isEmpty()) {
            alertaRepository.save(Alerta.builder()
                    .loteId(state.getLoteId())
                    .tipoAlerta(type)
                    .diasRestantes(days)
                    .generadaEn(OffsetDateTime.now(clock))
                    .atendida(false)
                    .build());
        }
    }

    private static String text(JsonNode node, String name) {
        JsonNode value = node.get(name);
        return value == null || value.isNull() ? "" : value.asText();
    }

    private static String requiredText(JsonNode node, String name) {
        String value = text(node, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Falta el campo " + name);
        }
        return value;
    }

    private static Long requiredLong(JsonNode node, String name) {
        JsonNode value = node.get(name);
        if (value == null || !value.canConvertToLong()) {
            throw new IllegalArgumentException("Falta el campo " + name);
        }
        return value.longValue();
    }

    private static Integer requiredInt(JsonNode node, String name) {
        JsonNode value = node.get(name);
        if (value == null || !value.canConvertToInt()) {
            throw new IllegalArgumentException("Falta el campo " + name);
        }
        return value.intValue();
    }

    private LoteEstadoResponse toResponse(LoteEstado state) {
        return new LoteEstadoResponse(state.getId(), state.getLoteId(), state.getStockActual(),
                state.getEstado(), state.getDiasParaVencer(), state.getActualizadoEn());
    }

    private AlertaResponse toResponse(Alerta alert) {
        return new AlertaResponse(alert.getId(), alert.getLoteId(), alert.getTipoAlerta(),
                alert.getDiasRestantes(), alert.getGeneradaEn(), alert.isAtendida());
    }
}
