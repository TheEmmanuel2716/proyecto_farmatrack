package com.farmatrack.inventario.service;
import com.farmatrack.inventario.dto.*;
import org.springframework.data.domain.*;
public interface LoteService {
    LoteResponse create(Long productoId, LoteInput input);
    Page<LoteResponse> findAll(Pageable pageable);
    LoteResponse findById(Long id);
}
