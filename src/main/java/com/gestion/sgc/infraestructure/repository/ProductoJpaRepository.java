package com.gestion.sgc.infraestructure.repository;

import com.gestion.sgc.infraestructure.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long> {

    Optional<ProductoEntity> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<ProductoEntity> findByTipoProductoTipoProductoId(Long tipoProductoId);

    @Query("SELECT p FROM ProductoEntity p WHERE p.estadoProducto = 'ACTIVO'")
    List<ProductoEntity> findAllActivos();

    @Query("SELECT p FROM ProductoEntity p WHERE p.stockMinimo > :stockActual")
    List<ProductoEntity> findByStockActualLessThan(@Param("stockActual") Integer stockActual);

    @Query("SELECT p FROM ProductoEntity p WHERE p.tipoProducto.tipoProductoId = :tipoId AND p.estadoProducto = 'ACTIVO'")
    List<ProductoEntity> findActivosByTipo(@Param("tipoId") Long tipoId);


}


