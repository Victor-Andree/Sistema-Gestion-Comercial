package com.gestion.sgc.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VentaResponse {

    private Long ventaId;
    private String clienteNombre;
    private String usuarioNombre;
    private LocalDateTime fechaVenta;
    private Double total;
    private String estado;
    private String comprobanteCorrelativo;

}
