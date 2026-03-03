package com.gestion.sgc.infraestructure.adapters;


import com.gestion.sgc.domain.aggregates.model.Comprobante;
import com.gestion.sgc.domain.aggregates.model.DetalleVenta;
import com.gestion.sgc.domain.aggregates.model.Venta;
import com.gestion.sgc.domain.ports.outputs.VentaRepositoryPort;
import com.gestion.sgc.infraestructure.entity.*;
import com.gestion.sgc.infraestructure.mapper.ComprobanteMapper;
import com.gestion.sgc.infraestructure.mapper.DetalleVentaMapper;
import com.gestion.sgc.infraestructure.mapper.ProductoMapper;
import com.gestion.sgc.infraestructure.mapper.VentaMapper;
import com.gestion.sgc.infraestructure.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VentaRepositoryAdapter implements VentaRepositoryPort {

    private final VentaJpaRepository ventaJpaRepository;
    private final DetalleVentaJpaRepository detalleJpaRepository;
    private final ComprobanteJpaRepository comprobanteJpaRepository;
    private final VentaMapper ventaMapper;
    private final ProductoMapper productoMapper;
    private final DetalleVentaMapper detalleVentaMapper;
    private final ComprobanteMapper comprobanteMapper;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final ClienteJpaRepository clienteJpaRepository;

    @Override
    public Venta save(Venta venta) {

        VentaEntity entity = ventaMapper.toEntity(venta);

        UsuarioEntity usuarioEntity = usuarioJpaRepository.findById(
                venta.getUsuario().getUsuarioId()
        ).orElseThrow();

        ClienteEntity clienteEntity = clienteJpaRepository.findById(
                venta.getCliente().getClienteId()
        ).orElseThrow();

        entity.setUsuario(usuarioEntity);
        entity.setCliente(clienteEntity);

        if (entity.getDetalles() != null) {
            entity.getDetalles().forEach(detalle -> detalle.setVenta(entity));
        }

        VentaEntity saved = ventaJpaRepository.save(entity);

        return ventaMapper.toDomainFromEntity(saved);
    }

    @Override
    public Optional<Venta> findById(Long id) {
        return ventaJpaRepository.findById(id)
                .map(ventaMapper::toDomainFromEntity);
    }

    @Override
    public List<Venta> findAll() {
        return ventaJpaRepository.findAll().stream()
                .map(ventaMapper::toDomainFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin) {
        return ventaJpaRepository.findByFechaVentaBetween(inicio, fin).stream()
                .map(ventaMapper::toDomainFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Venta> findByClienteId(Long clienteId) {
        return ventaJpaRepository.findByClienteClienteId(clienteId).stream()
                .map(ventaMapper::toDomainFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Venta> findByUsuarioId(Long usuarioId) {
        return ventaJpaRepository.findByUsuarioUsuarioId(usuarioId).stream()
                .map(ventaMapper::toDomainFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        ventaJpaRepository.deleteById(id);
    }

    @Override
    public DetalleVenta saveDetalle(DetalleVenta detalle) {

        DetalleVentaEntity entity = detalleVentaMapper.toEntity(detalle);

        if (detalle.getVenta() != null) {
            VentaEntity ventaRef = new VentaEntity();
            ventaRef.setVentaId(detalle.getVenta().getVentaId());
            entity.setVenta(ventaRef);
        }

        DetalleVentaEntity saved = detalleJpaRepository.save(entity);

        return detalleVentaMapper.toDomainFromEntity(saved);
    }

    @Override
    public List<DetalleVenta> findDetallesByVentaId(Long ventaId) {
        return detalleJpaRepository.findByVentaVentaId(ventaId).stream()
                .map(detalleVentaMapper::toDomainFromEntity) // 🔥 CORREGIDO
                .collect(Collectors.toList());
    }

    @Override
    public Comprobante saveComprobante(Comprobante comprobante) {

        ComprobanteEntity entity = comprobanteMapper.toEntity(comprobante);

        if (comprobante.getVenta() != null) {
            VentaEntity ventaRef = new VentaEntity();
            ventaRef.setVentaId(comprobante.getVenta().getVentaId());
            entity.setVenta(ventaRef);
        }

        ComprobanteEntity saved = comprobanteJpaRepository.save(entity);

        return comprobanteMapper.toDomainFromEntity(saved);
    }

    @Override
    public Optional<Comprobante> findComprobanteByVentaId(Long ventaId) {
        return comprobanteJpaRepository.findByVentaVentaId(ventaId)
                .map(comprobanteMapper::toDomainFromEntity);
    }

    @Override
    public Optional<Comprobante> findComprobanteByCorrelativo(String correlativo) {
        return comprobanteJpaRepository.findByCorrelativo(correlativo)
                .map(comprobanteMapper::toDomainFromEntity);
    }

    @Override
    public Double sumTotalVentasByFecha(LocalDateTime inicio, LocalDateTime fin) {
        return ventaJpaRepository.sumTotalByFechaBetween(inicio, fin);
    }

    @Override
    public Long countVentasByFecha(LocalDateTime inicio, LocalDateTime fin) {
        return ventaJpaRepository.countByFechaBetween(inicio, fin);
    }


}
