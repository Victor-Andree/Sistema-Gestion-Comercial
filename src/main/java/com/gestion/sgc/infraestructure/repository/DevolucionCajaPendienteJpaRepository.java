package com.gestion.sgc.infraestructure.repository;

import com.gestion.sgc.infraestructure.entity.DevolucionCajaPendienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import com.gestion.sgc.domain.aggregates.constans.EstadoDevolucionCaja;

public interface DevolucionCajaPendienteJpaRepository extends JpaRepository<DevolucionCajaPendienteEntity, Long> {
    Optional<DevolucionCajaPendienteEntity> findByVentaId(Long ventaId);
    List<DevolucionCajaPendienteEntity> findAllByEstadoOrderByFechaRegistroAsc(EstadoDevolucionCaja estado);
}
