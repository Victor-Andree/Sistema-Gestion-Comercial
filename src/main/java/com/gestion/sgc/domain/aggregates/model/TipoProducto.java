package com.gestion.sgc.domain.aggregates.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TipoProducto {

    private Long tipoProductoId;
    private String nombre;

}
