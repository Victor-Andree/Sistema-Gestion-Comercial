package com.gestion.sgc.application.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponse {

    private Long usuarioId;
    private Long personaId;
    private String nombre;
    private String apellidos;
    private String username;
    private String rol;
    private String estado;

}
