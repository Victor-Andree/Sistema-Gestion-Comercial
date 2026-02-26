package com.gestion.sgc.domain.ports.outputs;

import com.gestion.sgc.domain.aggregates.model.Persona;

import java.util.List;
import java.util.Optional;

public interface PersonaRepositoryPort {

    Persona save(Persona persona);
    Optional<Persona> findById(Long id);
    Optional<Persona> findByDni(String dni);
    Optional<Persona> findByCorreo(String correo);
    List<Persona> findAll();
    boolean existsByDni(String dni);
    boolean existsByCorreo(String correo);
    void deleteById(Long id);

}
