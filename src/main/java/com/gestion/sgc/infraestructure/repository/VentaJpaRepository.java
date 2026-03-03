package com.gestion.sgc.infraestructure.repository;

import com.gestion.sgc.domain.aggregates.constans.EstadoVenta;
import com.gestion.sgc.infraestructure.entity.VentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaJpaRepository extends JpaRepository<VentaEntity, Long> {

    List<VentaEntity> findByFechaVentaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<VentaEntity> findByClienteClienteId(Long clienteId);

    List<VentaEntity> findByUsuarioUsuarioId(Long usuarioId);

    List<VentaEntity> findByEstado(EstadoVenta estado);


    List<VentaEntity> findByClienteClienteIdAndFechaVentaBetween(
            Long clienteId, LocalDateTime inicio, LocalDateTime fin);

    List<VentaEntity> findByUsuarioUsuarioIdAndFechaVentaBetween(
            Long usuarioId, LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM VentaEntity v " +
            "WHERE v.fechaVenta BETWEEN :inicio AND :fin AND v.estado = 'COMPLETADA'")
    Double sumTotalByFechaBetween(@Param("inicio") LocalDateTime inicio,
                                  @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(v) FROM VentaEntity v " +
            "WHERE v.fechaVenta BETWEEN :inicio AND :fin")
    Long countByFechaBetween(@Param("inicio") LocalDateTime inicio,
                             @Param("fin") LocalDateTime fin);

    @Query("SELECT v FROM VentaEntity v WHERE DATE(v.fechaVenta) = CURRENT_DATE")
    List<VentaEntity> findVentasDelDia();

    @Query("SELECT dv.producto.nombre, SUM(dv.cantidad) as total " +
            "FROM DetalleVentaEntity dv " +
            "GROUP BY dv.producto.productoId " +
            "ORDER BY total DESC")
    List<Object[]> findTopProductosVendidos();


}
