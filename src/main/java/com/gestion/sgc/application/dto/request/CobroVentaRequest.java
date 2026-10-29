package com.gestion.sgc.application.dto.request;

import com.gestion.sgc.domain.aggregates.constans.MetodoPago;
import jakarta.validation.constraints.NotNull;

public record CobroVentaRequest(
        @NotNull(message = "El método de pago es obligatorio") MetodoPago metodoPago,
        Long cajaId
) {}
