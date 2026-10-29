package com.gestion.sgc.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DevolucionCajaPendienteResponse(
        Long devolucionCajaId,
        Long ventaId,
        Long cajaId,
        Long movimientoCobroId,
        BigDecimal monto,
        String estado,
        LocalDateTime fechaRegistro,
        Long movimientoCompensacionId
) {}
