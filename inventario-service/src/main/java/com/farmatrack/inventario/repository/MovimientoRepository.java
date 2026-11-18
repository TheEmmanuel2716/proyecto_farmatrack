package com.farmatrack.inventario.repository;
import com.farmatrack.inventario.entity.Movimiento;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    Page<Movimiento> findByLoteId(Long loteId, Pageable pageable);
}
