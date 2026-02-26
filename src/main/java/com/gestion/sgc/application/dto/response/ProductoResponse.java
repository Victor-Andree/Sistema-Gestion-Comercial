package com.gestion.sgc.application.dto.response;

import com.gestion.sgc.domain.aggregates.constans.EstadoProducto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoResponse {

    private Long productoId;
    private String nombre;
    private String descripcion;
    private Double precioVenta;
    private int stockMinimo;
    private EstadoProducto estadoProducto;
    private Long tipoProductoId;
    private String tipoProductoNombre;


}
