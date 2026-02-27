package com.gestion.sgc.infraestructure.repository;

import com.gestion.sgc.infraestructure.entity.DetalleVentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetalleVentaJpaRepository extends JpaRepository<DetalleVentaEntity, Long> {

    List<DetalleVentaEntity> findByVentaVentaId(Long ventaId);

    List<DetalleVentaEntity> findByProductoProductoId(Long productoId);

    List<DetalleVentaEntity> findByVentaVentaIdAndProductoProductoId(
            Long ventaId, Long productoId);

    void deleteByVentaVentaId(Long ventaId);

    // Obtener subtotal de una venta
    @Query("SELECT SUM(dv.subtotal) FROM DetalleVentaEntity dv " +
            "WHERE dv.venta.ventaId = :ventaId")
    Double sumSubtotalByVentaId(@Param("ventaId") Long ventaId);

}
