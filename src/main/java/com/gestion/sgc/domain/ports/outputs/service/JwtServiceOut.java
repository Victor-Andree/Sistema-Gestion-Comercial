package com.gestion.sgc.domain.ports.outputs.service;

import com.gestion.sgc.infraestructure.entity.UsuarioEntity;

public interface JwtServiceOut {
    String generateToken(UsuarioEntity usuarioEntity);

}
