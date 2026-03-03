package com.gestion.sgc.domain.aggregates.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    private Long stockId;
    private Producto producto;
    private Integer cantidadActual;
    private LocalDateTime fechaActualizacion;

}
