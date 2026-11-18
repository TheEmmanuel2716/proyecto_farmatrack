package com.farmatrack.inventario.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proveedores", schema = "inventario", uniqueConstraints = {
        @UniqueConstraint(name = "uk_proveedor_nit", columnNames = "nit"),
        @UniqueConstraint(name = "uk_proveedor_nombre_nit", columnNames = {"nombre", "nit"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Proveedor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false, unique = true)
    private String nit;
    private String contacto;
}
