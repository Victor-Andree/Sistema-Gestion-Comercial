package com.gestion.sgc.infraestructure.repository;

import com.gestion.sgc.infraestructure.entity.DevolucionCajaPendienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DevolucionCajaPendienteJpaRepository extends JpaRepository<DevolucionCajaPendienteEntity, Long> {
    Optional<DevolucionCajaPendienteEntity> findByVentaId(Long ventaId);
}
