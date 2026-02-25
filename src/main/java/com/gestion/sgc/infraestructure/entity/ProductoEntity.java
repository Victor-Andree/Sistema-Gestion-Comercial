package com.gestion.sgc.infraestructure.entity;


import com.gestion.sgc.domain.aggregates.constans.EstadoProducto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Productos")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productoId;

    @Column(unique = true, nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String descripcion;

    @Column(unique = true, nullable = false)
    private Double precioVenta;

    @Column(unique = true, nullable = false)
    private int stockMinimo;

    @Enumerated(EnumType.STRING)
    private EstadoProducto estadoProducto;

    @ManyToOne
    @JoinColumn(name = "tipo_producto_id", nullable = false)
    private TipoProductoEntity tipoProducto;


}
