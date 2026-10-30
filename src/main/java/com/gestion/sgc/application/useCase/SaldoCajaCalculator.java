package com.gestion.sgc.application.useCase;

import com.gestion.sgc.application.dto.response.ResumenCajaResponse;
import com.gestion.sgc.domain.aggregates.constans.TipoMovimientoCaja;
import com.gestion.sgc.domain.aggregates.model.SesionCaja;
import com.gestion.sgc.domain.ports.outputs.MovimientoCajaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class SaldoCajaCalculator {
    private final MovimientoCajaRepositoryPort movimientos;

    public ResumenCajaResponse resumen(SesionCaja sesion) {
        BigDecimal ingresos = total(sesion.getSesionCajaId(), TipoMovimientoCaja.INGRESO);
        BigDecimal egresos = total(sesion.getSesionCajaId(), TipoMovimientoCaja.EGRESO);
        return new ResumenCajaResponse(sesion.getSesionCajaId(), sesion.getMontoInicial(), ingresos, egresos,
                sesion.getMontoInicial().add(ingresos).subtract(egresos));
    }

    public BigDecimal saldo(SesionCaja sesion) {
        return resumen(sesion).saldoProvisional();
    }

    private BigDecimal total(Long sesionId, TipoMovimientoCaja tipo) {
        BigDecimal total = movimientos.sumBySesionIdAndTipo(sesionId, tipo);
        return (total == null ? BigDecimal.ZERO : total).setScale(2, RoundingMode.UNNECESSARY);
    }
}
