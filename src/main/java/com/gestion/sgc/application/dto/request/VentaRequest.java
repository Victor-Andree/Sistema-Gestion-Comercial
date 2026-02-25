package com.gestion.sgc.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VentaRequest {

    private Long usuarioId;
    private Long clienteId;
    private List<DetalleVentaRequest> detalles;

}
