package com.gestion.sgc.domain.aggregates.model;

import com.gestion.sgc.domain.aggregates.constans.EstadoSesionCaja;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SesionCaja {
    private Long sesionCajaId;
    private Caja caja;
    private EstadoSesionCaja estado;
    private BigDecimal montoInicial;
    private LocalDateTime fechaApertura;
    private Usuario usuarioApertura;
}
