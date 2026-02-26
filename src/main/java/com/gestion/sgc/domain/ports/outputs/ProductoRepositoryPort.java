package com.gestion.sgc.domain.ports.outputs;

import com.gestion.sgc.domain.aggregates.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepositoryPort {

    Producto save(Producto producto);

    Optional<Producto> findById(Long id);

    List<Producto> findAll();

    List<Producto> findByTipoProductoId(Long tipoProductoId);

    List<Producto> findByStockBajo(Integer stockMinimo);

    Optional<Producto> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    boolean existsById(Long id);

    void deleteById(Long id);



}
