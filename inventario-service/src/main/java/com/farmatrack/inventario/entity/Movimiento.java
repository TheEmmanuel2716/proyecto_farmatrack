package com.farmatrack.inventario.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "movimientos", schema = "inventario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Movimiento {
    public enum Tipo { INGRESO, VENTA, DEVOLUCION }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Tipo tipo;
    @Column(nullable = false)
    private Integer cantidad;
    @Column(name = "fecha_hora", nullable = false)
    private OffsetDateTime fechaHora;
    @Column(name = "evento_uuid", nullable = false, unique = true)
    private UUID eventoUuid;
    private String observacion;
    @PrePersist
    void onCreate() {
        if (fechaHora == null) fechaHora = OffsetDateTime.now();
        if (eventoUuid == null) eventoUuid = UUID.randomUUID();
    }
}
