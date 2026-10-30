package com.gestion.sgc.domain.ports.inputs.auth;

import com.gestion.sgc.application.dto.request.ActualizarUsuarioRequest;
import com.gestion.sgc.application.dto.request.CambiarEstadoUsuarioRequest;
import com.gestion.sgc.application.dto.request.RegistrarUsuarioRequest;
import com.gestion.sgc.application.dto.response.UsuarioResponse;

import java.util.List;

public interface AdministrarUsuarioIn {
    List<UsuarioResponse> listar();
    UsuarioResponse crear(RegistrarUsuarioRequest request);
    UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request, String actor);
    UsuarioResponse cambiarEstado(Long id, CambiarEstadoUsuarioRequest request, String actor);
}
