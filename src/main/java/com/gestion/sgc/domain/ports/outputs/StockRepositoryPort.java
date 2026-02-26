package com.gestion.sgc.domain.ports.outputs;

import com.gestion.sgc.domain.aggregates.model.Stock;

import java.util.List;
import java.util.Optional;

public interface StockRepositoryPort {

    Stock save(Stock stock);
    Optional<Stock> findById(Long id);
    Optional<Stock> findByProductoId(Long productoId);
    List<Stock> findAll();
    void deleteById(Long id);
    List<Stock> findByStockBajo();
    boolean existsByProductoId(Long productoId);



}
