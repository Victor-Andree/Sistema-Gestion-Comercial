package com.gestion.sgc.infraestructure.repository;

import com.gestion.sgc.domain.aggregates.constans.EstadoSesionCaja;
import com.gestion.sgc.infraestructure.entity.SesionCajaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SesionCajaJpaRepository extends JpaRepository<SesionCajaEntity, Long> {
    @EntityGraph(attributePaths = {"caja", "usuarioApertura"})
    Optional<SesionCajaEntity> findFirstByCajaCajaIdAndEstado(Long cajaId, EstadoSesionCaja estado);
}
