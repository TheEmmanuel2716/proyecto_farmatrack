package com.farmatrack.vencimientos.repository;

import com.farmatrack.vencimientos.entity.EventoProcesado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventoProcesadoRepository extends JpaRepository<EventoProcesado, UUID> {
}
