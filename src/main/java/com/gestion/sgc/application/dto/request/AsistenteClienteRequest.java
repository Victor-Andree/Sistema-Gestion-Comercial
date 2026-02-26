package com.gestion.sgc.application.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsistenteClienteRequest {

    private String nombre;
    private String apellidos;
    private String dni;
    private String correo;
    private String telefono;

    private LocalDateTime fechaRegistro;



}
