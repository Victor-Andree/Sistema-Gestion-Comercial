package com.gestion.sgc.infraestructure.adapters;


import com.gestion.sgc.domain.aggregates.model.Comprobante;
import com.gestion.sgc.domain.aggregates.model.DetalleVenta;
import com.gestion.sgc.domain.aggregates.model.Venta;
import com.gestion.sgc.domain.ports.outputs.VentaRepositoryPort;
import com.gestion.sgc.infraestructure.entity.ComprobanteEntity;
import com.gestion.sgc.infraestructure.entity.DetalleVentaEntity;
import com.gestion.sgc.infraestructure.entity.VentaEntity;
import com.gestion.sgc.infraestructure.mapper.ProductoMapper;
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
    private final ProductoMapper productoMapper;

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
        DetalleVentaEntity entity = new DetalleVentaEntity();

        // Campos básicos , asignacion directa , corrigiendo el bug de precio unitario
        entity.setCantidad(detalle.getCantidad());
        entity.setPrecioUnitario(detalle.getPrecioUnitario());
        entity.setSubtotal(detalle.getSubtotal());

        // Relaciones con mappers específicos
        if (detalle.getProducto() != null) {
            entity.setProducto(productoMapper.toEntity(detalle.getProducto()));
        }

        if (detalle.getVenta() != null) {

            entity.setVenta(ventaMapper.toEntity(detalle.getVenta()));
        }

        // Guardar
        DetalleVentaEntity saved = detalleJpaRepository.save(entity);

        //  MAPEO INVERSO
        DetalleVenta resultado = new DetalleVenta();
        resultado.setDetalleVentaId(saved.getDetalleVentaId());
        resultado.setCantidad(saved.getCantidad());
        resultado.setPrecioUnitario(saved.getPrecioUnitario());
        resultado.setSubtotal(saved.getSubtotal());

        if (saved.getProducto() != null) {
            resultado.setProducto(productoMapper.toDomainFromEntity(saved.getProducto()));
        }

        if (saved.getVenta() != null) {
            resultado.setVenta(ventaMapper.toDomainFromEntity(saved.getVenta()));
        }

        return resultado;
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
