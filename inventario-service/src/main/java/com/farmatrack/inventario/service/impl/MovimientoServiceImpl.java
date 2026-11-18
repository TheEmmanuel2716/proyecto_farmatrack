package com.farmatrack.inventario.service.impl;
import com.farmatrack.inventario.dto.*;
import com.farmatrack.inventario.entity.*;
import com.farmatrack.inventario.event.*;
import com.farmatrack.inventario.exception.*;
import com.farmatrack.inventario.repository.MovimientoRepository;
import com.farmatrack.inventario.service.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
@Service @RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {
    private final LoteServiceImpl loteService;
    private final MovimientoRepository movimientoRepository;
    private final KafkaEventPublisher publisher;
    public Page<MovimientoResponse> findByLote(Long loteId, Pageable p) {
        loteService.get(loteId);
        return movimientoRepository.findByLoteId(loteId, p).map(this::toResponse);
    }
    @Transactional
    public MovimientoResponse sell(Long loteId, MovimientoInput input) {
        return register(loteId, input, Movimiento.Tipo.VENTA);
    }
    @Transactional
    public MovimientoResponse returnToSupplier(Long loteId, MovimientoInput input) {
        return register(loteId, input, Movimiento.Tipo.DEVOLUCION);
    }
    private MovimientoResponse register(Long loteId, MovimientoInput input, Movimiento.Tipo type) {
        Lote lote = loteService.get(loteId);
        if (type == Movimiento.Tipo.VENTA && lote.getFechaVencimiento().isBefore(LocalDate.now()))
            throw new LoteVencidoException("No es posible vender un lote vencido.");
        if (input.cantidad() > lote.getStockActual())
            throw new StockInsuficienteException("La cantidad solicitada supera el stock disponible.");
        lote.setStockActual(lote.getStockActual() - input.cantidad());
        Movimiento m = movimientoRepository.save(Movimiento.builder().lote(lote).tipo(type)
                .cantidad(input.cantidad()).observacion(input.observacion()).build());
        if (type == Movimiento.Tipo.VENTA) {
            publisher.publish(loteId, new MovimientoVentaEvent(m.getEventoUuid(), loteId, m.getCantidad(), lote.getStockActual(), m.getFechaHora(), "MOVIMIENTO_VENTA"));
        } else {
            publisher.publish(loteId, new MovimientoDevolucionEvent(m.getEventoUuid(), loteId, m.getCantidad(), lote.getStockActual(), m.getFechaHora(), "MOVIMIENTO_DEVOLUCION"));
        }
        return toResponse(m);
    }
    private MovimientoResponse toResponse(Movimiento m) { return new MovimientoResponse(m.getId(), m.getLote().getId(), m.getTipo().name(), m.getCantidad(), m.getFechaHora(), m.getEventoUuid()); }
}
