package com.gestion.sgc.infraestructure.security.services.impl;

import com.gestion.sgc.infraestructure.security.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtServiceImpl implements JwtService {

    @Value("${key-signature}")
    private String keySignature;

    @Override
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ 1000 * 60 * 60 * 10))
                .compact();
    }

    private Key getKey() {
        byte [] keyBytes = Decoders.BASE64.decode(keySignature);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUserName(token);

        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    @Override
    public String extractUserName(String token) {
        return getClaim(token,Claims::getSubject);
    }

    private Claims getClaims(String token){
        return  Jwts
                .parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private  <T> T  getClaim(String token, Function<Claims, T> claimsResults){
        final  Claims claims = getClaims(token);

        return  claimsResults.apply(claims);
    }

    private boolean isTokenExpired(String token){
        final  Claims claims = getClaims(token);

        return claims.getExpiration().before(new Date());
    }


}
