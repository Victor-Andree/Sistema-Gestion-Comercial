package com.gestion.sgc.infraestructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "tipo_producto")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TipoProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tipoProductoId;

    @Column(unique = true, nullable = false)
    private String nombre;

    @OneToMany(mappedBy = "tipoProducto")
    private List<ProductoEntity> productos;

}
