package com.gestion.sgc.domain.ports.outputs;

import com.gestion.sgc.domain.aggregates.model.Caja;

import java.util.List;
import java.util.Optional;

public interface CajaRepositoryPort {
    List<Caja> findAll();
    Optional<Caja> findByIdForUpdate(Long cajaId);
}
