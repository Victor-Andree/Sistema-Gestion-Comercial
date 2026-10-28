package com.gestion.sgc.infraestructure.repository;

import com.gestion.sgc.domain.aggregates.constans.EstadoSesionCaja;
import com.gestion.sgc.infraestructure.entity.SesionCajaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface SesionCajaJpaRepository extends JpaRepository<SesionCajaEntity, Long> {
    @EntityGraph(attributePaths = {"caja", "usuarioApertura"})
    Optional<SesionCajaEntity> findFirstByCajaCajaIdAndEstado(Long cajaId, EstadoSesionCaja estado);

    @EntityGraph(attributePaths = {"caja", "usuarioApertura"})
    @Query("select s from SesionCajaEntity s where s.sesionCajaId = :id")
    Optional<SesionCajaEntity> findWithDetailsById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"caja", "usuarioApertura"})
    @Query("select s from SesionCajaEntity s where s.sesionCajaId = :id")
    Optional<SesionCajaEntity> findByIdForUpdate(@Param("id") Long id);
}
