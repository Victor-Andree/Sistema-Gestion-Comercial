package com.gestion.sgc.infraestructure.adapters;

import com.gestion.sgc.domain.aggregates.model.TipoProducto;
import com.gestion.sgc.domain.ports.outputs.TipoProductoRepositoryPort;
import com.gestion.sgc.infraestructure.entity.TipoProductoEntity;
import com.gestion.sgc.infraestructure.mapper.TipoProductoMapper;
import com.gestion.sgc.infraestructure.repository.TipoProductoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TipoProductoRepositoryAdapter implements TipoProductoRepositoryPort {

    private final TipoProductoJpaRepository jpaRepository;
    private final TipoProductoMapper tipoProductoMapper;

    @Override
    public TipoProducto save(TipoProducto tipoProducto) {
        TipoProductoEntity entity = tipoProductoMapper.toEntity(tipoProducto);
        TipoProductoEntity saved = jpaRepository.save(entity);
        return tipoProductoMapper.toDomainFromEntity(saved);
    }

    @Override
    public Optional<TipoProducto> findById(Long id) {
        return jpaRepository.findById(id)
                .map(tipoProductoMapper::toDomainFromEntity);
    }

    @Override
    public Optional<TipoProducto> findByNombre(String nombre) {
        return jpaRepository.findByNombre(nombre)
                .map(tipoProductoMapper::toDomainFromEntity);
    }

    @Override
    public List<TipoProducto> findAll() {
        return jpaRepository.findAll().stream()
                .map(tipoProductoMapper::toDomainFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return jpaRepository.existsByNombre(nombre);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

}
