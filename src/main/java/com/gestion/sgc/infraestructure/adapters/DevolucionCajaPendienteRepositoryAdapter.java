package com.gestion.sgc.infraestructure.adapters;

import com.gestion.sgc.domain.aggregates.model.DevolucionCajaPendiente;
import com.gestion.sgc.domain.ports.outputs.DevolucionCajaPendienteRepositoryPort;
import com.gestion.sgc.infraestructure.entity.DevolucionCajaPendienteEntity;
import com.gestion.sgc.infraestructure.repository.DevolucionCajaPendienteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DevolucionCajaPendienteRepositoryAdapter implements DevolucionCajaPendienteRepositoryPort {
    private final DevolucionCajaPendienteJpaRepository repository;

    @Override
    public DevolucionCajaPendiente save(DevolucionCajaPendiente devolucion) {
        DevolucionCajaPendienteEntity entity = toEntity(devolucion);
        return toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<DevolucionCajaPendiente> findByVentaId(Long ventaId) {
        return repository.findByVentaId(ventaId).map(this::toDomain);
    }

    private DevolucionCajaPendienteEntity toEntity(DevolucionCajaPendiente value) {
        DevolucionCajaPendienteEntity entity = value.getDevolucionCajaId() == null
                ? new DevolucionCajaPendienteEntity()
                : repository.findById(value.getDevolucionCajaId()).orElseGet(DevolucionCajaPendienteEntity::new);
        entity.setDevolucionCajaId(value.getDevolucionCajaId());
        entity.setVentaId(value.getVentaId());
        entity.setCajaId(value.getCajaId());
        entity.setMovimientoCobroId(value.getMovimientoCobroId());
        entity.setMonto(value.getMonto());
        entity.setEstado(value.getEstado());
        entity.setFechaRegistro(value.getFechaRegistro());
        entity.setMovimientoCompensacionId(value.getMovimientoCompensacionId());
        return entity;
    }

    private DevolucionCajaPendiente toDomain(DevolucionCajaPendienteEntity entity) {
        return new DevolucionCajaPendiente(entity.getDevolucionCajaId(), entity.getVentaId(),
                entity.getCajaId(), entity.getMovimientoCobroId(), entity.getMonto(), entity.getEstado(),
                entity.getFechaRegistro(), entity.getMovimientoCompensacionId());
    }
}
