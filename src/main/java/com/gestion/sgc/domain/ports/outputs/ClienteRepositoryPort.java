package com.gestion.sgc.domain.ports.outputs;

import com.gestion.sgc.domain.aggregates.model.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepositoryPort {

    Cliente save(Cliente cliente);

    Optional<Cliente> findById(Long id);

    Optional<Cliente> findByPersonaId(Long personaId);

    Optional<Cliente> findByDni(String dni);

    List<Cliente> findAll();

    boolean existsByPersonaId(Long personaId);

    boolean existsByDni(String dni);

    void deleteById(Long id);


}
