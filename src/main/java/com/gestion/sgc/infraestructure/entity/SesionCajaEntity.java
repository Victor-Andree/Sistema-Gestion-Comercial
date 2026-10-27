package com.gestion.sgc.infraestructure.entity;

import com.gestion.sgc.domain.aggregates.constans.EstadoSesionCaja;
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
@Table(name = "sesiones_caja")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SesionCajaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sesion_caja_id")
    private Long sesionCajaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caja_id", nullable = false)
    private CajaEntity caja;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoSesionCaja estado;

    @Column(name = "monto_inicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoInicial;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_apertura_id", nullable = false)
    private UsuarioEntity usuarioApertura;
}
