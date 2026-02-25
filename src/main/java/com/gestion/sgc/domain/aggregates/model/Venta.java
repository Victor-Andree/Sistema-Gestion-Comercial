package com.gestion.sgc.domain.aggregates.model;

import com.gestion.sgc.domain.aggregates.constans.EstadoVenta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Venta {

    private Long ventaId;
    private Usuario usuario;
    private Cliente cliente;
    private LocalDateTime fechaVenta;
    private Double total;
    private EstadoVenta estado;
    private List<DetalleVenta> detalles;
    private Comprobante comprobante;

}
