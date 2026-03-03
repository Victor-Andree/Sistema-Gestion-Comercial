package com.gestion.sgc.domain.aggregates.model;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Persona {

    private Long personaId;
    private String nombre;
    private String apellidos;
    private String dni;
    private String correo;
    private String telefono;

}
