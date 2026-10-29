package com.gestion.sgc.infraestructure.entity;

import com.gestion.sgc.domain.aggregates.constans.EstadoDevolucionCaja;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "devoluciones_caja_pendientes")
@Data
@NoArgsConstructor
public class DevolucionCajaPendienteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "devolucion_caja_id")
    private Long devolucionCajaId;

    @Column(name = "venta_id", nullable = false, unique = true)
    private Long ventaId;

    @Column(name = "caja_id", nullable = false)
    private Long cajaId;

    @Column(name = "movimiento_cobro_id", nullable = false)
    private Long movimientoCobroId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoDevolucionCaja estado;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "movimiento_compensacion_id")
    private Long movimientoCompensacionId;
}
