package com.gestion.sgc.domain.ports.outputs;

import com.gestion.sgc.domain.aggregates.model.SesionCaja;

import java.util.Optional;

public interface SesionCajaRepositoryPort {
    SesionCaja save(SesionCaja sesion);
    Optional<SesionCaja> findOpenByCajaId(Long cajaId);
    Optional<SesionCaja> findById(Long sesionId);
    Optional<SesionCaja> findByIdForUpdate(Long sesionId);
}
