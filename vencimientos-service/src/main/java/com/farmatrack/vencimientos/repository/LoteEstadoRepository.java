package com.farmatrack.vencimientos.repository;

import com.farmatrack.vencimientos.entity.LoteEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoteEstadoRepository extends JpaRepository<LoteEstado, Long> {
    Optional<LoteEstado> findByLoteId(Long loteId);
}
