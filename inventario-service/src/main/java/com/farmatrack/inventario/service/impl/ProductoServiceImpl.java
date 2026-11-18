package com.farmatrack.inventario.service.impl;
import com.farmatrack.inventario.dto.*;
import com.farmatrack.inventario.entity.*;
import com.farmatrack.inventario.exception.DatoDuplicadoException;
import com.farmatrack.inventario.exception.RecursoNoEncontradoException;
import com.farmatrack.inventario.repository.*;
import com.farmatrack.inventario.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
@Service @RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {
    private final ProductoRepository repository;
    private final ProveedorRepository proveedorRepository;

    public ProductoResponse create(ProductoInput i) {
        Proveedor proveedor = proveedorRepository.findById(i.proveedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado: " + i.proveedorId()));
        validarProductoDuplicado(i, null, proveedor.getId());
        return toResponse(repository.save(Producto.builder().nombre(normalizarTexto(i.nombre())).principioActivo(normalizarTexto(i.principioActivo())).proveedor(proveedor).build()));
    }

    public ProductoResponse update(Long id, ProductoInput i) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + id));
        Proveedor proveedor = proveedorRepository.findById(i.proveedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado: " + i.proveedorId()));
        validarProductoDuplicado(i, id, proveedor.getId());
        producto.setNombre(normalizarTexto(i.nombre()));
        producto.setPrincipioActivo(normalizarTexto(i.principioActivo()));
        producto.setProveedor(proveedor);
        return toResponse(repository.save(producto));
    }

    public Page<ProductoResponse> findAll(Pageable p) { return repository.findAll(p).map(this::toResponse); }
    public ProductoResponse findById(Long id) { return toResponse(repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + id))); }
    private ProductoResponse toResponse(Producto p) { return new ProductoResponse(p.getId(), p.getNombre(), p.getPrincipioActivo(), p.getProveedor().getId()); }

    private void validarProductoDuplicado(ProductoInput i, Long idExcluido, Long proveedorId) {
        String nombre = normalizarTexto(i.nombre());
        String principioActivo = normalizarTexto(i.principioActivo());
        boolean existe = idExcluido == null
                ? repository.existsByNombreIgnoreCaseAndPrincipioActivoIgnoreCaseAndProveedorId(nombre, principioActivo, proveedorId)
                : repository.existsByNombreIgnoreCaseAndPrincipioActivoIgnoreCaseAndProveedorIdAndIdNot(nombre, principioActivo, proveedorId, idExcluido);
        if (existe) {
            throw new DatoDuplicadoException("Ya existe un producto con el mismo nombre y principio activo para este proveedor.");
        }
    }

    private String normalizarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }
}
