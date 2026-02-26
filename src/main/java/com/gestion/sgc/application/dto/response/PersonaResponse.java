package com.gestion.sgc.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonaResponse {

    private Long personaId;
    private String nombre;
    private String apellidos;
    private String dni;
    private String correo;
    private String telefono;

}
