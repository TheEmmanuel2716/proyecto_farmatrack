package com.farmatrack.inventario.service.impl;
import com.farmatrack.inventario.dto.*;
import com.farmatrack.inventario.entity.*;
import com.farmatrack.inventario.event.*;
import com.farmatrack.inventario.exception.DatoDuplicadoException;
import com.farmatrack.inventario.exception.RecursoNoEncontradoException;
import com.farmatrack.inventario.repository.*;
import com.farmatrack.inventario.service.LoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
@Service @RequiredArgsConstructor
public class LoteServiceImpl implements LoteService {
    private final LoteRepository loteRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoRepository movimientoRepository;
    private final KafkaEventPublisher publisher;
    @Transactional
    public LoteResponse create(Long productoId, LoteInput i) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));
        validarLoteDuplicado(i.numeroLote(), productoId, null);
        Lote lote = loteRepository.save(Lote.builder().numeroLote(normalizarTexto(i.numeroLote())).fechaVencimiento(i.fechaVencimiento())
                .cantidadInicial(i.cantidadInicial()).stockActual(i.cantidadInicial()).producto(producto).build());
        Movimiento movimiento = movimientoRepository.save(Movimiento.builder().lote(lote).tipo(Movimiento.Tipo.INGRESO)
                .cantidad(i.cantidadInicial()).build());
        publisher.publish(lote.getId(), new LoteIngresadoEvent(movimiento.getEventoUuid(), lote.getId(), producto.getId(),
                lote.getFechaVencimiento(), lote.getStockActual(), "LOTE_INGRESADO"));
        return toResponse(lote);
    }
    public Page<LoteResponse> findAll(Pageable p) { return loteRepository.findAll(p).map(this::toResponse); }
    public LoteResponse findById(Long id) { return toResponse(get(id)); }
    public Lote get(Long id) { return loteRepository.findDetailedById(id).orElseThrow(() -> new RecursoNoEncontradoException("Lote no encontrado: " + id)); }
    private LoteResponse toResponse(Lote l) { return new LoteResponse(l.getId(), l.getNumeroLote(), l.getFechaVencimiento(), l.getCantidadInicial(), l.getProducto().getId(), l.getStockActual(), l.getCreadoEn()); }

    private void validarLoteDuplicado(String numeroLote, Long productoId, Long idExcluido) {
        String valor = normalizarTexto(numeroLote);
        boolean existe = idExcluido == null
                ? loteRepository.existsByNumeroLoteIgnoreCaseAndProductoId(valor, productoId)
                : loteRepository.existsByNumeroLoteIgnoreCaseAndProductoIdAndIdNot(valor, productoId, idExcluido);
        if (existe) {
            throw new DatoDuplicadoException("Ya existe un lote con ese número para este producto.");
        }
    }

    private String normalizarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }
}
