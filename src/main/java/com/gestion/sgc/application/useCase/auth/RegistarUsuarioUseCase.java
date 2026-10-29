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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.gestion.sgc.domain.aggregates.constans.RolEnum;

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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una persona con el DNI indicado");
        }

        if (personaRepository.existsByCorreo(request.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una persona con el correo indicado");
        }

        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El username ya está en uso");
        }

        String rol;
        try {
            rol = RolEnum.valueOf(request.getRol().trim().toUpperCase()).name();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol debe ser ADMIN o ASISTENTE");
        }

        Persona persona = personaMapper.toDomainFromRegistroRequest(request);
        Persona personaGuardada = personaRepository.save(persona);

        Usuario usuario = new Usuario();
        usuario.setPersona(personaGuardada);
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEstadoUsuario("ACTIVO");
        usuario.setRol(rol);
        usuario.setFechaRegistro(LocalDateTime.now());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return usuarioMapper.toResponse(usuarioGuardado, personaGuardada);


    }


}
