package com.gestion.sgc.domain.ports.outputs;

import com.gestion.sgc.domain.aggregates.constans.TipoMovimientoCaja;
import com.gestion.sgc.domain.aggregates.model.MovimientoCaja;

import java.math.BigDecimal;
import java.util.List;

public interface MovimientoCajaRepositoryPort {
    MovimientoCaja save(MovimientoCaja movimiento);
    List<MovimientoCaja> findAllBySesionId(Long sesionId);
    BigDecimal sumBySesionIdAndTipo(Long sesionId, TipoMovimientoCaja tipo);
}
