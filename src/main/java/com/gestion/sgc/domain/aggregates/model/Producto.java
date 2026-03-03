package com.gestion.sgc.domain.aggregates.model;

import com.gestion.sgc.domain.aggregates.constans.EstadoProducto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    private Long productoId;
    private String nombre;
    private String descripcion;
    private Double precioVenta;
    private int stockMinimo;
    private EstadoProducto estadoProducto;
    private TipoProducto tipoProducto;

}
