package com.farmatrack.inventario.repository;
import com.farmatrack.inventario.entity.Lote;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
public interface LoteRepository extends JpaRepository<Lote, Long> {
    @Query("select l from Lote l join fetch l.producto where l.id = :id")
    Optional<Lote> findDetailedById(Long id);
    boolean existsByNumeroLoteIgnoreCaseAndProductoId(String numeroLote, Long productoId);
    boolean existsByNumeroLoteIgnoreCaseAndProductoIdAndIdNot(String numeroLote, Long productoId, Long id);
}
