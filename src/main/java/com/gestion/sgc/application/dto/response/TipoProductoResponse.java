package com.gestion.sgc.application.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TipoProductoResponse {

    private Long tipoProductoId;
    private String nombre;


}
