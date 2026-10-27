package com.gestion.sgc.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AperturaCajaRequest(
        @NotNull(message = "El monto inicial es obligatorio")
        @DecimalMin(value = "0.00", message = "El monto inicial no puede ser negativo")
        @Digits(integer = 13, fraction = 2, message = "El monto inicial admite hasta 13 enteros y 2 decimales")
        BigDecimal montoInicial
) {}
