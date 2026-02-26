package com.gestion.sgc.infraestructure.repository;

import com.gestion.sgc.infraestructure.entity.PersonaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonaJpaRepository extends JpaRepository<PersonaEntity, Long> {

    Optional<PersonaEntity> findByDni(String dni);

    Optional<PersonaEntity> findByCorreo(String correo);

    boolean existsByDni(String dni);

    boolean existsByCorreo(String correo);

}
