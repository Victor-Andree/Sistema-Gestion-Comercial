package com.gestion.sgc.application.dto.response;

import java.math.BigDecimal;

public record ResumenCajaResponse(
        Long sesionCajaId,
        BigDecimal montoInicial,
        BigDecimal totalIngresos,
        BigDecimal totalEgresos,
        BigDecimal saldoProvisional
) {}
