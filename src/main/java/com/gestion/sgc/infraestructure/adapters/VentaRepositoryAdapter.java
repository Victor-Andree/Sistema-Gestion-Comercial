package com.gestion.sgc.infraestructure.adapters;


import com.gestion.sgc.domain.aggregates.model.Comprobante;
import com.gestion.sgc.domain.aggregates.model.DetalleVenta;
import com.gestion.sgc.domain.aggregates.model.Venta;
import com.gestion.sgc.domain.ports.outputs.VentaRepositoryPort;
import com.gestion.sgc.infraestructure.entity.ComprobanteEntity;
import com.gestion.sgc.infraestructure.entity.DetalleVentaEntity;
import com.gestion.sgc.infraestructure.entity.VentaEntity;
import com.gestion.sgc.infraestructure.mapper.VentaMapper;
import com.gestion.sgc.infraestructure.repository.ComprobanteJpaRepository;
import com.gestion.sgc.infraestructure.repository.DetalleVentaJpaRepository;
import com.gestion.sgc.infraestructure.repository.VentaJpaRepository;
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

    @Override
    public Venta save(Venta venta) {
        VentaEntity entity = ventaMapper.toEntity(venta);
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
        DetalleVentaEntity entity = ventaMapper.toEntity(detalle);
        DetalleVentaEntity saved = detalleJpaRepository.save(entity);
        return ventaMapper.toDomainFromEntity(saved);
    }

    @Override
    public List<DetalleVenta> findDetallesByVentaId(Long ventaId) {
        return detalleJpaRepository.findByVentaVentaId(ventaId).stream()
                .map(ventaMapper::toDomainFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Comprobante saveComprobante(Comprobante comprobante) {
        ComprobanteEntity entity = ventaMapper.toEntity(comprobante);
        ComprobanteEntity saved = comprobanteJpaRepository.save(entity);
        return ventaMapper.toDomainFromEntity(saved);
    }

    @Override
    public Optional<Comprobante> findComprobanteByVentaId(Long ventaId) {
        return comprobanteJpaRepository.findByVentaVentaId(ventaId)
                .map(ventaMapper::toDomainFromEntity);
    }

    @Override
    public Optional<Comprobante> findComprobanteByCorrelativo(String correlativo) {
        return comprobanteJpaRepository.findByCorrelativo(correlativo)
                .map(ventaMapper::toDomainFromEntity);
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
