package com.gestion.sgc.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SesionCajaResponse(
        Long sesionCajaId,
        Long cajaId,
        String cajaNombre,
        String estado,
        BigDecimal montoInicial,
        LocalDateTime fechaApertura,
        Long usuarioAperturaId,
        String usuarioApertura
) {}
