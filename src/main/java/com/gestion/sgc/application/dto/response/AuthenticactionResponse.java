package com.gestion.sgc.application.dto.response;

public class AuthenticactionResponse {

    private String token;

    private String nombre;

    public AuthenticactionResponse(String token, String nombre) {
        this.token = token;
        this.nombre = nombre;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
