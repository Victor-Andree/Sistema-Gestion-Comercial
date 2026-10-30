package com.gestion.sgc.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CierreCajaRequest(
        @NotNull(message = "El monto contado es obligatorio")
        @DecimalMin(value = "0.00", message = "El monto contado no puede ser negativo")
        @Digits(integer = 13, fraction = 2, message = "El monto contado admite hasta 13 enteros y 2 decimales")
        BigDecimal montoContado,
        @Size(max = 500, message = "La observación admite hasta 500 caracteres")
        String observacion
) {}
