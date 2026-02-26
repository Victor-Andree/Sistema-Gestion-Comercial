package com.gestion.sgc.infraestructure.adapters;

import com.gestion.sgc.domain.aggregates.model.Cliente;
import com.gestion.sgc.domain.ports.outputs.ClienteRepositoryPort;
import com.gestion.sgc.infraestructure.entity.ClienteEntity;
import com.gestion.sgc.infraestructure.mapper.ClienteMapper;
import com.gestion.sgc.infraestructure.repository.ClienteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

    private final ClienteJpaRepository jpaRepository;
    private final ClienteMapper clienteMapper;

    @Override
    public Cliente save(Cliente cliente) {
        ClienteEntity entity = clienteMapper.toEntity(cliente);
        ClienteEntity saved = jpaRepository.save(entity);
        return clienteMapper.toDomainFromEntity(saved);
    }

    @Override
    public Optional<Cliente> findById(Long id) {
        return jpaRepository.findById(id)
                .map(clienteMapper::toDomainFromEntity);
    }

    @Override
    public Optional<Cliente> findByPersonaId(Long personaId) {
        return jpaRepository.findByPersonaPersonaId(personaId)
                .map(clienteMapper::toDomainFromEntity);
    }

    @Override
    public Optional<Cliente> findByDni(String dni) {
        return jpaRepository.findByPersonaDni(dni)
                .map(clienteMapper::toDomainFromEntity);
    }

    @Override
    public List<Cliente> findAll() {
        return jpaRepository.findAll().stream()
                .map(clienteMapper::toDomainFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByPersonaId(Long personaId) {
        return jpaRepository.existsByPersonaPersonaId(personaId);
    }

    @Override
    public boolean existsByDni(String dni) {
        return jpaRepository.existsByPersonaDni(dni);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

}
