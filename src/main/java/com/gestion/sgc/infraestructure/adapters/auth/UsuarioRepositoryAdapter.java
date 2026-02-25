package com.gestion.sgc.infraestructure.adapters.auth;

import com.gestion.sgc.domain.aggregates.model.Usuario;
import com.gestion.sgc.domain.ports.outputs.auth.UsuarioRepositoryPort;
import com.gestion.sgc.infraestructure.entity.UsuarioEntity;
import com.gestion.sgc.infraestructure.mapper.UsuarioMapper;
import com.gestion.sgc.infraestructure.repository.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository jpaRepository;
    private final UsuarioMapper usuarioMapper;



    @Override
    public Optional<Usuario> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(usuarioMapper::toDomainFromEntity);
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity entity = usuarioMapper.toEntity(usuario);
        UsuarioEntity saved = jpaRepository.save(entity);
        return usuarioMapper.toDomainFromEntity(saved);
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return jpaRepository.findById(id)
                .map(usuarioMapper::toDomainFromEntity);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }


}
