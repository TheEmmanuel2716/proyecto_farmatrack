package com.farmatrack.inventario.service;
import com.farmatrack.inventario.dto.*;
import org.springframework.data.domain.*;
public interface ProductoService {
    ProductoResponse create(ProductoInput input);
    ProductoResponse update(Long id, ProductoInput input);
    Page<ProductoResponse> findAll(Pageable pageable);
    ProductoResponse findById(Long id);
}
