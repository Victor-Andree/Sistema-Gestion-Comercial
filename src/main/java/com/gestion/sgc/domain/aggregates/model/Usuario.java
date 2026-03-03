package com.gestion.sgc.domain.aggregates.model;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Long usuarioId;
    private Persona persona;
    private String username;
    private String password;
    private String estadoUsuario;
    private String rol;
    private LocalDateTime fechaRegistro;

}
