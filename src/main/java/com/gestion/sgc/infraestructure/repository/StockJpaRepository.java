package com.gestion.sgc.infraestructure.repository;

import com.gestion.sgc.infraestructure.entity.StockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StockJpaRepository extends JpaRepository<StockEntity, Long> {

    Optional<StockEntity> findByProductoProductoId(Long productoId);

    boolean existsByProductoProductoId(Long productoId);

    @Query("SELECT s FROM StockEntity s WHERE s.cantidadActual <= s.producto.stockMinimo")
    List<StockEntity> findStockBajo();

    @Query("SELECT s FROM StockEntity s WHERE s.cantidadActual = 0")
    List<StockEntity> findStockAgotado();

    @Query("SELECT s FROM StockEntity s ORDER BY s.cantidadActual ASC")
    List<StockEntity> findAllOrderByCantidadAsc();



}
