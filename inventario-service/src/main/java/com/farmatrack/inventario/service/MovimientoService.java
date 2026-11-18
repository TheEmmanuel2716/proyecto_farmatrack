package com.farmatrack.inventario.service;
import com.farmatrack.inventario.dto.*;
import org.springframework.data.domain.*;
public interface MovimientoService {
    Page<MovimientoResponse> findByLote(Long loteId, Pageable pageable);
    MovimientoResponse sell(Long loteId, MovimientoInput input);
    MovimientoResponse returnToSupplier(Long loteId, MovimientoInput input);
}
