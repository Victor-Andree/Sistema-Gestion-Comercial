package com.gestion.sgc.infraestructure.entity;

import com.gestion.sgc.domain.aggregates.constans.TipoMovimientoCaja;
import com.gestion.sgc.domain.aggregates.constans.OrigenMovimientoCaja;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_caja")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoCajaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movimiento_caja_id")
    private Long movimientoCajaId;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 64)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sesion_caja_id", nullable = false)
    private SesionCajaEntity sesionCaja;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimientoCaja tipo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 200)
    private String concepto;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fechaMovimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_responsable_id", nullable = false)
    private UsuarioEntity usuarioResponsable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrigenMovimientoCaja origen = OrigenMovimientoCaja.MANUAL;

    @Column(name = "venta_id")
    private Long ventaId;

    @Column(name = "movimiento_original_id")
    private Long movimientoOriginalId;
}
