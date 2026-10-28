package com.gestion.sgc.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoCajaResponse(
        Long movimientoCajaId,
        Long sesionCajaId,
        String tipo,
        BigDecimal monto,
        String concepto,
        LocalDateTime fechaMovimiento,
        Long usuarioResponsableId,
        String usuarioResponsable
) {}
