package com.gestion.sgc.application.useCase;

import com.gestion.sgc.application.dto.request.PersonaRequest;
import com.gestion.sgc.application.dto.response.PersonaResponse;
import com.gestion.sgc.domain.aggregates.model.Persona;
import com.gestion.sgc.domain.ports.inputs.PersonaIn;
import com.gestion.sgc.domain.ports.outputs.PersonaRepositoryPort;
import com.gestion.sgc.infraestructure.mapper.PersonaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonaUseCase implements PersonaIn {

    private final PersonaRepositoryPort personaRepository;
    private final PersonaMapper personaMapper;

    @Override
    public PersonaResponse crearPersona(PersonaRequest request) {
        // Validaciones
        if (personaRepository.existsByDni(request.getDni())) {
            throw new RuntimeException("Ya existe una persona con el DNI: " + request.getDni());
        }
        if (personaRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("Ya existe una persona con el correo: " + request.getCorreo());
        }

        // Request → Domain (usando mapper)
        Persona persona = personaMapper.toDomainFromRequest(request);

        // Guardar
        Persona personaGuardada = personaRepository.save(persona);

        // Domain → Response (usando mapper)
        return personaMapper.toResponse(personaGuardada);
    }

    @Override
    public Optional<PersonaResponse> buscarPorId(Long id) {
        return personaRepository.findById(id)
                .map(personaMapper::toResponse);  // ← Usando mapper
    }

    @Override
    public List<PersonaResponse> listarTodas() {
        return personaRepository.findAll().stream()
                .map(personaMapper::toResponse)  // ← Usando mapper
                .collect(Collectors.toList());
    }


}
