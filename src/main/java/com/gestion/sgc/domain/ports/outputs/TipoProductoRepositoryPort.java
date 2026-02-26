package com.gestion.sgc.domain.ports.outputs;

import com.gestion.sgc.domain.aggregates.model.TipoProducto;

import java.util.List;
import java.util.Optional;

public interface TipoProductoRepositoryPort {

    TipoProducto save(TipoProducto tipoProducto);

    Optional<TipoProducto> findById(Long id);

    Optional<TipoProducto> findByNombre(String nombre);

    List<TipoProducto> findAll();

    boolean existsByNombre(String nombre);

    boolean existsById(Long id);

    void deleteById(Long id);

}
