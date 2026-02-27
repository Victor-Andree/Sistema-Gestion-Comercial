package com.gestion.sgc.infraestructure.repository;

import com.gestion.sgc.infraestructure.entity.ComprobanteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ComprobanteJpaRepository extends JpaRepository<ComprobanteEntity, Long> {

    Optional<ComprobanteEntity> findByVentaVentaId(Long ventaId);

    Optional<ComprobanteEntity> findByCorrelativo(String correlativo);

    List<ComprobanteEntity> findByTipo(String tipo);

    List<ComprobanteEntity> findByEstado(String estado);

    List<ComprobanteEntity> findByFechaEmisionBetween(LocalDateTime inicio, LocalDateTime fin);

    List<ComprobanteEntity> findByTipoAndFechaEmisionBetween(
            String tipo, LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT c FROM ComprobanteEntity c " +
            "WHERE c.correlativo LIKE :prefijo% " +
            "ORDER BY c.correlativo DESC")
    List<ComprobanteEntity> findLastCorrelativoByPrefijo(@Param("prefijo") String prefijo);

    // Verificar si existe comprobante para una venta
    boolean existsByVentaVentaId(Long ventaId);

}
