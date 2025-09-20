package com.gestion.sgc.domain.ports.outputs.auth;

import com.gestion.sgc.domain.aggregates.model.Usuario;
import com.gestion.sgc.infraestructure.entity.UsuarioEntity;

public interface AuthenticationOut {

    Usuario registrarUsuario(Usuario trabajador);

    void login(UsuarioEntity usuarioEntity);

}
