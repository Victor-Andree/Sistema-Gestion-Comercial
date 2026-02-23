package com.gestion.sgc.infraestructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TiposProductos")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TipoProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tipoProductoId;

    @Column(unique = true, nullable = false)
    private String nombre;
}
