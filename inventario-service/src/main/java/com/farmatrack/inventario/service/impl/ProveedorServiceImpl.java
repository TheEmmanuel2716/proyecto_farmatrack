package com.farmatrack.inventario.service.impl;
import com.farmatrack.inventario.dto.*;
import com.farmatrack.inventario.entity.Proveedor;
import com.farmatrack.inventario.exception.DatoDuplicadoException;
import com.farmatrack.inventario.exception.RecursoNoEncontradoException;
import com.farmatrack.inventario.repository.ProveedorRepository;
import com.farmatrack.inventario.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
@Service @RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {
    private final ProveedorRepository repository;

    public ProveedorResponse create(ProveedorInput i) {
        String nombre = normalizarTexto(i.nombre());
        String nit = normalizarTexto(i.nit());
        validarDuplicado(nombre, nit, null);
        return toResponse(repository.save(Proveedor.builder().nombre(nombre).nit(nit).contacto(i.contacto()).build()));
    }

    public ProveedorResponse update(Long id, ProveedorInput i) {
        Proveedor proveedor = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado: " + id));
        String nombre = normalizarTexto(i.nombre());
        String nit = normalizarTexto(i.nit());
        validarDuplicado(nombre, nit, id);
        proveedor.setNombre(nombre);
        proveedor.setNit(nit);
        proveedor.setContacto(i.contacto());
        return toResponse(repository.save(proveedor));
    }

    public Page<ProveedorResponse> findAll(Pageable p) { return repository.findAll(p).map(this::toResponse); }

    private ProveedorResponse toResponse(Proveedor p) { return new ProveedorResponse(p.getId(), p.getNombre(), p.getNit(), p.getContacto()); }

    private void validarDuplicado(String nombre, String nit, Long idExcluido) {
        validarNitDuplicado(nit, idExcluido);
        boolean existe = idExcluido == null
                ? repository.existsByNombreIgnoreCaseAndNitIgnoreCase(nombre, nit)
                : repository.existsByNombreIgnoreCaseAndNitIgnoreCaseAndIdNot(nombre, nit, idExcluido);
        if (existe) {
            throw new DatoDuplicadoException("Ya existe un proveedor con el mismo nombre y NIT.");
        }
    }

    private void validarNitDuplicado(String nit, Long idExcluido) {
        String valor = normalizarTexto(nit);
        boolean existe = idExcluido == null ? repository.existsByNit(valor) : repository.existsByNitAndIdNot(valor, idExcluido);
        if (existe) {
            throw new DatoDuplicadoException("El NIT ya está registrado para otro proveedor.");
        }
    }

    private String normalizarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }
}
