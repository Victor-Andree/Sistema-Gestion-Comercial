package com.gestion.sgc.infraestructure.repository;

import com.gestion.sgc.domain.aggregates.constans.TipoMovimientoCaja;
import com.gestion.sgc.infraestructure.entity.MovimientoCajaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface MovimientoCajaJpaRepository extends JpaRepository<MovimientoCajaEntity, Long> {
    @EntityGraph(attributePaths = {"sesionCaja", "usuarioResponsable"})
    List<MovimientoCajaEntity> findAllBySesionCajaSesionCajaIdOrderByFechaMovimientoDesc(Long sesionId);

    @Query("select coalesce(sum(m.monto), 0) from MovimientoCajaEntity m " +
            "where m.sesionCaja.sesionCajaId = :sesionId and m.tipo = :tipo")
    BigDecimal sumBySesionIdAndTipo(@Param("sesionId") Long sesionId,
                                    @Param("tipo") TipoMovimientoCaja tipo);
}
