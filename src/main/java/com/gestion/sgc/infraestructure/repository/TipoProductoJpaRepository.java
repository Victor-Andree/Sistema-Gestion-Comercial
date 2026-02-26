package com.gestion.sgc.infraestructure.repository;

import com.gestion.sgc.infraestructure.entity.TipoProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoProductoJpaRepository extends JpaRepository<TipoProductoEntity , Long> {

    Optional<TipoProductoEntity> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

}
