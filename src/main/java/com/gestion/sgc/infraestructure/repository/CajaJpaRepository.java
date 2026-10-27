package com.gestion.sgc.infraestructure.repository;

import com.gestion.sgc.infraestructure.entity.CajaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CajaJpaRepository extends JpaRepository<CajaEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CajaEntity c where c.cajaId = :id")
    Optional<CajaEntity> findByIdForUpdate(@Param("id") Long id);
}
