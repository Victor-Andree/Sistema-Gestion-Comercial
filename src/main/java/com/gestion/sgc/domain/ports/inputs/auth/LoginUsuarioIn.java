package com.gestion.sgc.domain.ports.inputs.auth;

import com.gestion.sgc.application.dto.request.LoginRequest;
import com.gestion.sgc.application.dto.response.AuthenticactionResponse;

public interface LoginUsuarioIn {

    AuthenticactionResponse login(LoginRequest request);

}
