package com.farmatrack.inventario.service;
import com.farmatrack.inventario.dto.*;
import org.springframework.data.domain.*;
public interface ProveedorService {
    ProveedorResponse create(ProveedorInput input);
    ProveedorResponse update(Long id, ProveedorInput input);
    Page<ProveedorResponse> findAll(Pageable pageable);
}
