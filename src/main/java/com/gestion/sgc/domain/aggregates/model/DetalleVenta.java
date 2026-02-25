package com.gestion.sgc.domain.aggregates.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetalleVenta {

    private Long detalleVentaId;
    private Producto producto;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;

}
