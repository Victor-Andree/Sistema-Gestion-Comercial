package com.gestion.sgc.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class PersonaRequest {

    private String nombre;
    private String apellidos;
    private String dni;
    private String correo;
    private String telefono;

}
