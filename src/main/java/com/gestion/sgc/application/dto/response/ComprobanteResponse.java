package com.gestion.sgc.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComprobanteResponse {

    private Long comprobanteId;
    private String tipo;
    private String correlativo;
    private String estado;
    private LocalDateTime fechaEmision;

}
