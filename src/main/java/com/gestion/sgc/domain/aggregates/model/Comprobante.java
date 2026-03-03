package com.gestion.sgc.domain.aggregates.model;

import com.gestion.sgc.domain.aggregates.constans.EstadoComprobante;
import com.gestion.sgc.domain.aggregates.constans.TipoComprobante;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comprobante {

    private Long comprobanteId;
    private Venta venta;
    private TipoComprobante tipo;
    private String correlativo;
    private EstadoComprobante estado;
    private LocalDateTime fechaEmision;

}
