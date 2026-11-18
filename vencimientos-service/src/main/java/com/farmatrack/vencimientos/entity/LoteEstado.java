package com.farmatrack.vencimientos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "lotes_estado", schema = "vencimientos",
        uniqueConstraints = @UniqueConstraint(name = "uk_lote_estado_lote", columnNames = "lote_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoteEstado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "lote_id", nullable = false, unique = true)
    private Long loteId;
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVencimiento estado;
    @Column(name = "dias_para_vencer", nullable = false)
    private Integer diasParaVencer;
    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;
}
