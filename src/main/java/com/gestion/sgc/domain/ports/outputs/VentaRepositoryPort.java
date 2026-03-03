package com.gestion.sgc.domain.ports.outputs;

import com.gestion.sgc.domain.aggregates.constans.EstadoVenta;
import com.gestion.sgc.domain.aggregates.model.Comprobante;
import com.gestion.sgc.domain.aggregates.model.DetalleVenta;
import com.gestion.sgc.domain.aggregates.model.Venta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaRepositoryPort {

    Venta save(Venta venta);
    Optional<Venta> findById(Long id);
    List<Venta> findAll();
    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
    List<Venta> findByClienteId(Long clienteId);
    List<Venta> findByUsuarioId(Long usuarioId);
    List<Venta> findByEstado(EstadoVenta estado);
    void deleteById(Long id);

    DetalleVenta saveDetalle(DetalleVenta detalle);
    List<DetalleVenta> findDetallesByVentaId(Long ventaId);

    Comprobante saveComprobante(Comprobante comprobante);
    Optional<Comprobante> findComprobanteByVentaId(Long ventaId);
    Optional<Comprobante> findComprobanteByCorrelativo(String correlativo);

    Double sumTotalVentasByFecha(LocalDateTime inicio, LocalDateTime fin);
    Long countVentasByFecha(LocalDateTime inicio, LocalDateTime fin);


}
