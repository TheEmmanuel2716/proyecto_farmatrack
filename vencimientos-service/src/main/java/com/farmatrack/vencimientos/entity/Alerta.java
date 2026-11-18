package com.farmatrack.vencimientos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "alertas", schema = "vencimientos",
        uniqueConstraints = @UniqueConstraint(name = "uk_alerta_lote_tipo",
                columnNames = {"lote_id", "tipo_alerta"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "lote_id", nullable = false)
    private Long loteId;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alerta", nullable = false)
    private TipoAlerta tipoAlerta;
    @Column(name = "dias_restantes", nullable = false)
    private Integer diasRestantes;
    @Column(name = "generada_en", nullable = false)
    private OffsetDateTime generadaEn;
    @Column(nullable = false)
    private boolean atendida;
}
