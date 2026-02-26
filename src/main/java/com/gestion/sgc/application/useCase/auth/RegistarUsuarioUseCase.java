package com.gestion.sgc.application.useCase.auth;

import com.gestion.sgc.application.dto.request.RegistrarUsuarioRequest;
import com.gestion.sgc.application.dto.response.UsuarioResponse;
import com.gestion.sgc.domain.aggregates.model.Persona;
import com.gestion.sgc.domain.aggregates.model.Usuario;
import com.gestion.sgc.domain.ports.inputs.auth.RegistrarUsuarioIn;
import com.gestion.sgc.domain.ports.outputs.PersonaRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.auth.UsuarioRepositoryPort;
import com.gestion.sgc.infraestructure.mapper.PersonaMapper;
import com.gestion.sgc.infraestructure.mapper.UsuarioMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegistarUsuarioUseCase implements RegistrarUsuarioIn {

    private final PersonaRepositoryPort personaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PersonaMapper personaMapper;
    private final UsuarioMapper usuarioMapper;

    @Override
    @Transactional
    public UsuarioResponse registrar(RegistrarUsuarioRequest request) {

        if (personaRepository.existsByDni(request.getDni())) {
            throw new RuntimeException("Ya existe una persona con el DNI: " + request.getDni());
        }

        if (personaRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("Ya existe una persona con el correo: " + request.getCorreo());
        }

        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El username '" + request.getUsername() + "' ya está en uso");
        }

        Persona persona = personaMapper.toDomainFromRegistroRequest(request);
        Persona personaGuardada = personaRepository.save(persona);

        Usuario usuario = new Usuario();
        usuario.setPersona(personaGuardada);
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEstadoUsuario("ACTIVO");
        usuario.setRol(request.getRol());
        usuario.setFechaRegistro(LocalDateTime.now());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return usuarioMapper.toResponse(usuarioGuardado, personaGuardada);


    }


}
