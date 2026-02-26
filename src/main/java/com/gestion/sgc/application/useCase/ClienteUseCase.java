package com.gestion.sgc.application.useCase;


import com.gestion.sgc.application.dto.request.ClienteRequest;
import com.gestion.sgc.application.dto.response.ClienteResponse;
import com.gestion.sgc.domain.aggregates.model.Cliente;
import com.gestion.sgc.domain.aggregates.model.Persona;
import com.gestion.sgc.domain.ports.inputs.ClienteIn;
import com.gestion.sgc.domain.ports.outputs.ClienteRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.PersonaRepositoryPort;
import com.gestion.sgc.infraestructure.mapper.ClienteMapper;
import com.gestion.sgc.infraestructure.mapper.PersonaMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteUseCase implements ClienteIn {

    private final PersonaRepositoryPort personaRepository;
    private final ClienteRepositoryPort clienteRepository;
    private final PersonaMapper personaMapper;
    private final ClienteMapper clienteMapper;

    @Override
    @Transactional
    public ClienteResponse createCliente(ClienteRequest request) {
        // 1. Validar que no exista persona con mismo DNI
        if (personaRepository.existsByDni(request.getDni())) {
            throw new RuntimeException("Ya existe una persona con el DNI: " + request.getDni());
        }

        // 2. Validar que no exista persona con mismo correo
        if (personaRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("Ya existe una persona con el correo: " + request.getCorreo());
        }

        if (clienteRepository.existsByDni(request.getDni())) {
            throw new RuntimeException("Ya existe un cliente con el DNI: " + request.getDni());
        }

        Persona persona = personaMapper.toDomainFromClienteRequest(request);
        Persona personaGuardada = personaRepository.save(persona);

        Cliente cliente = new Cliente();
        cliente.setPersona(personaGuardada);
        cliente.setFechaRegistro(LocalDateTime.now());

        Cliente clienteGuardado = clienteRepository.save(cliente);

        return clienteMapper.toResponse(clienteGuardado, personaGuardada);
    }

    @Override
    public Optional<ClienteResponse> findById(Long id) {
        return clienteRepository.findById(id)
                .map(cliente -> {
                    Persona persona = cliente.getPersona();
                    return clienteMapper.toResponse(cliente, persona);
                });
    }

    @Override
    public List<ClienteResponse> getAllClientes() {
        return clienteRepository.findAll().stream()
                .map(cliente -> {
                    Persona persona = cliente.getPersona();
                    return clienteMapper.toResponse(cliente, persona);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ClienteResponse> findByDni(String dni) {
        return clienteRepository.findByDni(dni)
                .map(cliente -> {
                    Persona persona = cliente.getPersona();
                    return clienteMapper.toResponse(cliente, persona);
                });
    }

    @Override
    @Transactional
    public void deleteCliente(Long id) {
        if (!clienteRepository.existsByPersonaId(id)) {
            throw new RuntimeException("Cliente no encontrado con ID: " + id);
        }
        clienteRepository.deleteById(id);
    }



}
