package com.gestion.sgc.domain.ports.outputs.auth;

import com.gestion.sgc.domain.aggregates.model.Usuario;

import java.util.Optional;

public interface UsuarioRepositoryPort {

    Optional<Usuario> findByUsername(String username);
    Usuario save(Usuario usuario);
    Optional<Usuario> findById(Long id);
    boolean existsByUsername(String username);


}
