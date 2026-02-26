package com.gestion.sgc.application.dto.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockResponse {

    private Long stockId;
    private Long productoId;
    private String productoNombre;
    private Integer cantidadActual;
    private Integer stockMinimo;
    private LocalDateTime fechaActualizacion;
    private String estadoStock;

}
