package com.gestion.sgc.domain.ports.inputs.auth;

import com.gestion.sgc.application.dto.request.RegistrarUsuarioRequest;
import com.gestion.sgc.application.dto.response.UsuarioResponse;

public interface RegistrarUsuarioIn {

    UsuarioResponse registrar(RegistrarUsuarioRequest request);


}
