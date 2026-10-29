package com.gestion.sgc.application.useCase.auth;

import com.gestion.sgc.application.dto.request.ActualizarUsuarioRequest;
import com.gestion.sgc.application.dto.request.CambiarEstadoUsuarioRequest;
import com.gestion.sgc.application.dto.request.RegistrarUsuarioRequest;
import com.gestion.sgc.application.dto.response.UsuarioResponse;
import com.gestion.sgc.domain.aggregates.constans.EstadoUsuario;
import com.gestion.sgc.domain.aggregates.constans.RolEnum;
import com.gestion.sgc.domain.aggregates.model.Persona;
import com.gestion.sgc.domain.aggregates.model.Usuario;
import com.gestion.sgc.domain.ports.inputs.auth.AdministrarUsuarioIn;
import com.gestion.sgc.domain.ports.inputs.auth.RegistrarUsuarioIn;
import com.gestion.sgc.domain.ports.outputs.PersonaRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.auth.UsuarioRepositoryPort;
import com.gestion.sgc.infraestructure.mapper.UsuarioMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdministrarUsuarioUseCase implements AdministrarUsuarioIn {
    private final UsuarioRepositoryPort usuarioRepository;
    private final PersonaRepositoryPort personaRepository;
    private final RegistrarUsuarioIn registrarUsuario;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(usuario -> usuarioMapper.toResponse(usuario, usuario.getPersona()))
                .sorted(Comparator.comparing(UsuarioResponse::getNombre, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(UsuarioResponse::getApellidos, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    @Transactional
    public UsuarioResponse crear(RegistrarUsuarioRequest request) {
        return registrarUsuario.registrar(request);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request, String actor) {
        Usuario usuario = obtenerUsuario(id);
        Persona persona = usuario.getPersona();
        validarIdentidadUnica(persona, request.getDni(), request.getCorreo());
        if (usuarioRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El username ya está en uso");
        }

        String rol = validarRol(request.getRol());
        String estado = validarEstado(request.getEstado());
        if (usuario.getUsername().equals(actor)
                && (!usuario.getRol().equals(rol) || !usuario.getEstadoUsuario().equals(estado))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No puedes cambiar el rol ni desactivar tu propia cuenta");
        }

        persona.setNombre(request.getNombre().trim());
        persona.setApellidos(request.getApellidos().trim());
        persona.setDni(request.getDni().trim());
        persona.setCorreo(request.getCorreo().trim().toLowerCase());
        persona.setTelefono(request.getTelefono().trim());
        Persona personaGuardada = personaRepository.save(persona);

        usuario.setPersona(personaGuardada);
        usuario.setUsername(request.getUsername().trim());
        usuario.setRol(rol);
        usuario.setEstadoUsuario(estado);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        Usuario guardado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(guardado, personaGuardada);
    }

    @Override
    @Transactional
    public UsuarioResponse cambiarEstado(Long id, CambiarEstadoUsuarioRequest request, String actor) {
        Usuario usuario = obtenerUsuario(id);
        String estado = validarEstado(request.getEstado());
        if (usuario.getUsername().equals(actor) && !EstadoUsuario.ACTIVO.name().equals(estado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes desactivar tu propia cuenta");
        }
        usuario.setEstadoUsuario(estado);
        Usuario guardado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(guardado, guardado.getPersona());
    }

    private Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private void validarIdentidadUnica(Persona actual, String dni, String correo) {
        personaRepository.findByDni(dni).filter(persona -> !persona.getPersonaId().equals(actual.getPersonaId()))
                .ifPresent(persona -> { throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ya existe una persona con el DNI indicado"); });
        personaRepository.findByCorreo(correo).filter(persona -> !persona.getPersonaId().equals(actual.getPersonaId()))
                .ifPresent(persona -> { throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ya existe una persona con el correo indicado"); });
    }

    private String validarRol(String value) {
        try {
            return RolEnum.valueOf(value.trim().toUpperCase()).name();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol debe ser ADMIN o ASISTENTE");
        }
    }

    private String validarEstado(String value) {
        try {
            return EstadoUsuario.valueOf(value.trim().toUpperCase()).name();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El estado debe ser ACTIVO, INACTIVO o BLOQUEADO");
        }
    }
}
