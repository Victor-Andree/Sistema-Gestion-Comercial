package com.gestion.sgc.domain.aggregates.model;

import com.gestion.sgc.domain.aggregates.constans.TipoMovimientoCaja;
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
public class MovimientoCaja {
    private Long movimientoCajaId;
    private String idempotencyKey;
    private SesionCaja sesionCaja;
    private TipoMovimientoCaja tipo;
    private BigDecimal monto;
    private String concepto;
    private LocalDateTime fechaMovimiento;
    private Usuario usuarioResponsable;
}
