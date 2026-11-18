package com.farmatrack.vencimientos.repository;

import com.farmatrack.vencimientos.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    Page<Alerta> findByAtendida(boolean atendida, Pageable pageable);
    Optional<Alerta> findByLoteIdAndTipoAlerta(Long loteId, TipoAlerta tipoAlerta);
}
