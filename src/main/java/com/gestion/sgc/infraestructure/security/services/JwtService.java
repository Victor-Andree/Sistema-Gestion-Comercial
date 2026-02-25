package com.gestion.sgc.infraestructure.security.services;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.Map;

public interface JwtService {

    String generateToken(UserDetails userDetails);

    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails); // Útil

    String generateRefreshToken(UserDetails userDetails);

    boolean validateToken(String token, UserDetails userDetails);

    String extractUserName(String token);

    Date extractExpiration(String token);

    boolean isTokenExpired(String token);

}
