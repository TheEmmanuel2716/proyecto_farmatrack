package com.farmatrack.vencimientos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "eventos_procesados", schema = "vencimientos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventoProcesado {
    @Id
    @Column(name = "evento_uuid", nullable = false)
    private UUID eventoUuid;
    @Column(name = "procesado_en", nullable = false)
    private OffsetDateTime procesadoEn;
}
