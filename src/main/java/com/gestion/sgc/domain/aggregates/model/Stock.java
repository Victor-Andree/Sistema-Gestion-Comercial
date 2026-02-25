package com.gestion.sgc.domain.aggregates.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Stock {

    private Long stockId;
    private Producto producto;
    private Integer cantidadActual;
    private LocalDateTime fechaActualizacion;

}
