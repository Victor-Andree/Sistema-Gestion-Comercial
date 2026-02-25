package com.gestion.sgc.infraestructure.entity;

import com.gestion.sgc.domain.aggregates.constans.EstadoComprobante;
import com.gestion.sgc.domain.aggregates.constans.TipoComprobante;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comprobantes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComprobanteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long comprobanteId;

    @OneToOne
    @JoinColumn(name = "venta_id", nullable = false, unique = true)
    private VentaEntity venta;

    @Enumerated(EnumType.STRING)
    private TipoComprobante tipo;

    @Column(nullable = false)
    private String correlativo;

    @Enumerated(EnumType.STRING)
    private EstadoComprobante estado;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

}
