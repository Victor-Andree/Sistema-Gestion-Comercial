package com.gestion.sgc.domain.ports.outputs;

import com.gestion.sgc.domain.aggregates.model.DevolucionCajaPendiente;

import java.util.Optional;
import java.util.List;

public interface DevolucionCajaPendienteRepositoryPort {
    DevolucionCajaPendiente save(DevolucionCajaPendiente devolucion);
    Optional<DevolucionCajaPendiente> findByVentaId(Long ventaId);
    List<DevolucionCajaPendiente> findAllPending();
}
