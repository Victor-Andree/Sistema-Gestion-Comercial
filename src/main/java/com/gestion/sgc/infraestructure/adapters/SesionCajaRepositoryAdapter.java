package com.gestion.sgc.infraestructure.adapters;

import com.gestion.sgc.domain.aggregates.constans.EstadoSesionCaja;
import com.gestion.sgc.domain.aggregates.model.SesionCaja;
import com.gestion.sgc.domain.ports.outputs.SesionCajaRepositoryPort;
import com.gestion.sgc.infraestructure.mapper.SesionCajaMapper;
import com.gestion.sgc.infraestructure.repository.SesionCajaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SesionCajaRepositoryAdapter implements SesionCajaRepositoryPort {
    private final SesionCajaJpaRepository repository;
    private final SesionCajaMapper mapper;

    @Override
    public SesionCaja save(SesionCaja sesion) {
        return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(sesion)));
    }

    @Override
    public Optional<SesionCaja> findOpenByCajaId(Long cajaId) {
        return repository.findFirstByCajaCajaIdAndEstado(cajaId, EstadoSesionCaja.ABIERTA).map(mapper::toDomain);
    }

    @Override
    public Optional<SesionCaja> findOpenByCajaIdForUpdate(Long cajaId) {
        return repository.findOpenByCajaIdForUpdate(cajaId, EstadoSesionCaja.ABIERTA).map(mapper::toDomain);
    }

    @Override
    public Optional<SesionCaja> findById(Long sesionId) {
        return repository.findWithDetailsById(sesionId).map(mapper::toDomain);
    }

    @Override
    public Optional<SesionCaja> findByIdForUpdate(Long sesionId) {
        return repository.findByIdForUpdate(sesionId).map(mapper::toDomain);
    }
}
