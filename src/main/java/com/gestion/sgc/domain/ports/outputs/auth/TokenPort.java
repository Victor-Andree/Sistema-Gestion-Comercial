package com.gestion.sgc.domain.ports.outputs.auth;

import com.gestion.sgc.domain.aggregates.model.Usuario;

public interface TokenPort {

    String generateToken(Usuario usuario);
    String generateRefreshToken(Usuario usuario);
    String extractUsername(String token);
    boolean validateToken(String token);
    Long getExpirationTime();

}
