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
public class SesionCaja {
    private Long sesionCajaId;
    private Caja caja;
    private EstadoSesionCaja estado;
    private BigDecimal montoInicial;
    private LocalDateTime fechaApertura;
    private Usuario usuarioApertura;
    private BigDecimal saldoEsperado;
    private BigDecimal montoContado;
    private BigDecimal diferencia;
    private LocalDateTime fechaCierre;
    private Usuario usuarioCierre;
    private String observacionCierre;

    public SesionCaja(Long sesionCajaId, Caja caja, EstadoSesionCaja estado, BigDecimal montoInicial,
                      LocalDateTime fechaApertura, Usuario usuarioApertura) {
        this.sesionCajaId = sesionCajaId;
        this.caja = caja;
        this.estado = estado;
        this.montoInicial = montoInicial;
        this.fechaApertura = fechaApertura;
        this.usuarioApertura = usuarioApertura;
    }
}
