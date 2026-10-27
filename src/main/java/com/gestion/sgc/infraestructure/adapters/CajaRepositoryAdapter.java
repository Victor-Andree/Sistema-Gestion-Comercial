package com.gestion.sgc.infraestructure.adapters;

import com.gestion.sgc.domain.aggregates.model.Caja;
import com.gestion.sgc.domain.ports.outputs.CajaRepositoryPort;
import com.gestion.sgc.infraestructure.mapper.CajaMapper;
import com.gestion.sgc.infraestructure.repository.CajaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CajaRepositoryAdapter implements CajaRepositoryPort {
    private final CajaJpaRepository repository;
    private final CajaMapper mapper;

    @Override
    public List<Caja> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Caja> findByIdForUpdate(Long cajaId) {
        return repository.findByIdForUpdate(cajaId).map(mapper::toDomain);
    }
}
