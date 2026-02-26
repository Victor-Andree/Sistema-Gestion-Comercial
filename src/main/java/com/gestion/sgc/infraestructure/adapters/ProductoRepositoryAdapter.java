package com.gestion.sgc.infraestructure.adapters;

import com.gestion.sgc.domain.aggregates.model.Producto;
import com.gestion.sgc.domain.ports.outputs.ProductoRepositoryPort;
import com.gestion.sgc.infraestructure.entity.ProductoEntity;
import com.gestion.sgc.infraestructure.mapper.ProductoMapper;
import com.gestion.sgc.infraestructure.repository.ProductoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductoRepositoryAdapter implements ProductoRepositoryPort {

    private final ProductoJpaRepository jpaRepository;
    private final ProductoMapper productoMapper;

    @Override
    public Producto save(Producto producto) {
        ProductoEntity entity = productoMapper.toEntity(producto);
        ProductoEntity saved = jpaRepository.save(entity);
        return productoMapper.toDomainFromEntity(saved);
    }

    @Override
    public Optional<Producto> findById(Long id) {
        return jpaRepository.findById(id)
                .map(productoMapper::toDomainFromEntity);
    }

    @Override
    public List<Producto> findAll() {
        return jpaRepository.findAll().stream()
                .map(productoMapper::toDomainFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Producto> findByTipoProductoId(Long tipoProductoId) {
        return jpaRepository.findByTipoProductoTipoProductoId(tipoProductoId).stream()
                .map(productoMapper::toDomainFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Producto> findByStockBajo(Integer stockMinimo) {
        return jpaRepository.findByStockActualLessThan(stockMinimo).stream()
                .map(productoMapper::toDomainFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Producto> findByNombre(String nombre) {
        return jpaRepository.findByNombre(nombre)
                .map(productoMapper::toDomainFromEntity);
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
