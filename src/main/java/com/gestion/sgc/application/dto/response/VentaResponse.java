package com.gestion.sgc.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VentaResponse {

    private Long ventaId;
    private Long usuarioId;
    private String usuarioNombre;
    private Long clienteId;
    private String clienteNombre;
    private LocalDateTime fechaVenta;
    private Double total;
    private String estado;
    private ComprobanteResponse comprobante;
    private List<DetalleVentaResponse> detalles;

}
