package com.gestion.sgc.application.dto.request;

import com.gestion.sgc.domain.aggregates.constans.TipoMovimientoCaja;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MovimientoCajaRequest(
        @NotNull(message = "El tipo de movimiento es obligatorio")
        TipoMovimientoCaja tipo,
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.00", inclusive = false, message = "El monto debe ser mayor que cero")
        @Digits(integer = 13, fraction = 2, message = "El monto admite hasta 13 enteros y 2 decimales")
        BigDecimal monto,
        @NotBlank(message = "El concepto es obligatorio")
        @Size(max = 200, message = "El concepto admite hasta 200 caracteres")
        String concepto
) {}
