package com.gestion.sgc.domain.aggregates.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVenta {

    private Long detalleVentaId;
    private Venta venta;
    private Producto producto;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;

}
