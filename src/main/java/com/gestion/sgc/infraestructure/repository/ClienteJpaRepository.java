package com.gestion.sgc.infraestructure.repository;

import com.gestion.sgc.infraestructure.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, Long> {

    Optional<ClienteEntity> findByPersonaPersonaId(Long personaId);

    Optional<ClienteEntity> findByPersonaDni(String dni);

    boolean existsByPersonaPersonaId(Long personaId);

    boolean existsByPersonaDni(String dni);


}
