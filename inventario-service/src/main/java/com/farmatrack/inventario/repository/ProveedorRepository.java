package com.farmatrack.inventario.repository;
import com.farmatrack.inventario.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    boolean existsByNit(String nit);
    boolean existsByNitAndIdNot(String nit, Long id);
    boolean existsByNombreIgnoreCaseAndNitIgnoreCase(String nombre, String nit);
    boolean existsByNombreIgnoreCaseAndNitIgnoreCaseAndIdNot(String nombre, String nit, Long id);
}
