package com.gestion.sgc.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUsuarioRequest {

    private String nombre;
    private String apellidos;
    private String dni;
    private String correo;
    private String telefono;

    private String username;
    private String password;
    private String rol;

}
