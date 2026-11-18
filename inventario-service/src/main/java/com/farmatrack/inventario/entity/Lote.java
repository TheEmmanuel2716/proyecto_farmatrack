package com.farmatrack.inventario.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name = "lotes", schema = "inventario", uniqueConstraints = {
        @UniqueConstraint(name = "uk_lote_numero_producto", columnNames = {"numero_lote", "producto_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Lote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "numero_lote", nullable = false)
    private String numeroLote;
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;
    @Column(name = "cantidad_inicial", nullable = false)
    private Integer cantidadInicial;
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;
    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    @PrePersist
    void onCreate() { if (creadoEn == null) creadoEn = OffsetDateTime.now(); }
}
