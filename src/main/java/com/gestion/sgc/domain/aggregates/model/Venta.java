package com.gestion.sgc.domain.aggregates.model;

import com.gestion.sgc.domain.aggregates.constans.EstadoVenta;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
