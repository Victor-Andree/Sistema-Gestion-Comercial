package com.gestion.sgc.infraestructure.adapters;

import com.gestion.sgc.domain.aggregates.model.Persona;
import com.gestion.sgc.domain.ports.outputs.PersonaRepositoryPort;
import com.gestion.sgc.infraestructure.entity.PersonaEntity;
import com.gestion.sgc.infraestructure.mapper.PersonaMapper;
import com.gestion.sgc.infraestructure.repository.PersonaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PersonaRepositoryAdapter implements PersonaRepositoryPort {

    private final PersonaJpaRepository jpaRepository;
    private final PersonaMapper personaMapper;

    @Override
    public Persona save(Persona persona) {
        PersonaEntity entity = personaMapper.toEntity(persona);
        PersonaEntity saved = jpaRepository.save(entity);
        return personaMapper.toDomainFromEntity(saved);
    }

    @Override
    public Optional<Persona> findById(Long id) {
        return jpaRepository.findById(id)
                .map(personaMapper::toDomainFromEntity);
    }

    @Override
    public Optional<Persona> findByDni(String dni) {
        return jpaRepository.findByDni(dni)
                .map(personaMapper::toDomainFromEntity);
    }

    @Override
    public Optional<Persona> findByCorreo(String correo) {
        return jpaRepository.findByCorreo(correo)
                .map(personaMapper::toDomainFromEntity);
    }

    @Override
    public List<Persona> findAll() {
        return jpaRepository.findAll().stream()
                .map(personaMapper::toDomainFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByDni(String dni) {
        return jpaRepository.existsByDni(dni);
    }

    @Override
    public boolean existsByCorreo(String correo) {
        return jpaRepository.existsByCorreo(correo);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

}
