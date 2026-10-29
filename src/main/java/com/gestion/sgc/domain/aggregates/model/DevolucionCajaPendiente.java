package com.gestion.sgc.domain.aggregates.model;

import com.gestion.sgc.domain.aggregates.constans.EstadoDevolucionCaja;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DevolucionCajaPendiente {
    private Long devolucionCajaId;
    private Long ventaId;
    private Long cajaId;
    private Long movimientoCobroId;
    private BigDecimal monto;
    private EstadoDevolucionCaja estado;
    private LocalDateTime fechaRegistro;
    private Long movimientoCompensacionId;
}
