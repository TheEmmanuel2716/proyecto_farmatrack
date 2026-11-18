package com.farmatrack.inventario.repository;
import com.farmatrack.inventario.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByNombreIgnoreCaseAndPrincipioActivoIgnoreCaseAndProveedorId(String nombre, String principioActivo, Long proveedorId);
    boolean existsByNombreIgnoreCaseAndPrincipioActivoIgnoreCaseAndProveedorIdAndIdNot(String nombre, String principioActivo, Long proveedorId, Long id);
}
