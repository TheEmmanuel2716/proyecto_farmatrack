package com.farmatrack.inventario.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "productos", schema = "inventario", uniqueConstraints = {
        @UniqueConstraint(name = "uk_producto_nombre_principio_proveedor",
                columnNames = {"nombre", "principio_activo", "proveedor_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nombre;
    @Column(name = "principio_activo", nullable = false)
    private String principioActivo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;
}
