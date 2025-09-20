package com.gestion.sgc.application.dto;

import com.gestion.sgc.domain.aggregates.constans.EstadoUsuario;
import com.gestion.sgc.domain.aggregates.constans.RolEnum;

import java.time.LocalDateTime;

public class UsuarioDto {

    private Long usuarioId;

    private String username;

    private String password;

    private EstadoUsuario estado;

    private LocalDateTime fechaRegistro;

    private RolEnum rol;

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public RolEnum getRol() {
        return rol;
    }

    public void setRol(RolEnum rol) {
        this.rol = rol;
    }
}
