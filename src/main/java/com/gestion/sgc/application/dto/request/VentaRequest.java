package com.gestion.sgc.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VentaRequest {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "El detalle de venta no puede estar vacío")
    @Size(min = 1, message = "Debe haber al menos un producto")
    private List<DetalleVentaRequest> detalles;

    private String tipoComprobante;

    private String observacion;

}
