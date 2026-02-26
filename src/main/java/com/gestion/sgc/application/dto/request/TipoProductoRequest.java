package com.gestion.sgc.application.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TipoProductoRequest {

    @NotBlank(message = "El nombre del tipo es obligatorio")
    private String nombre;



}
