package com.gestion.sgc.infraestructure.adapters;

import com.gestion.sgc.domain.aggregates.constans.TipoMovimientoCaja;
import com.gestion.sgc.domain.aggregates.model.MovimientoCaja;
import com.gestion.sgc.domain.ports.outputs.MovimientoCajaRepositoryPort;
import com.gestion.sgc.infraestructure.mapper.MovimientoCajaMapper;
import com.gestion.sgc.infraestructure.repository.MovimientoCajaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MovimientoCajaRepositoryAdapter implements MovimientoCajaRepositoryPort {
    private final MovimientoCajaJpaRepository repository;
    private final MovimientoCajaMapper mapper;

    @Override
    public MovimientoCaja save(MovimientoCaja movimiento) {
        return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(movimiento)));
    }

    @Override
    public List<MovimientoCaja> findAllBySesionId(Long sesionId) {
        return repository.findAllBySesionCajaSesionCajaIdOrderByFechaMovimientoDesc(sesionId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public BigDecimal sumBySesionIdAndTipo(Long sesionId, TipoMovimientoCaja tipo) {
        return repository.sumBySesionIdAndTipo(sesionId, tipo);
    }
}
