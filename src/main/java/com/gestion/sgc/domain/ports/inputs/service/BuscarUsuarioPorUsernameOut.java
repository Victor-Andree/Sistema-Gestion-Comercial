package com.gestion.sgc.domain.ports.inputs.service;

import com.gestion.sgc.application.dto.UsuarioDto;

import java.util.Optional;

public interface BuscarUsuarioPorUsernameOut {
    Optional<UsuarioDto> findUsuarioByUsername(String username);

}
