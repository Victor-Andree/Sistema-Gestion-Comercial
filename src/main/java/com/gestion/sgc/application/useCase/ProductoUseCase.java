package com.gestion.sgc.application.useCase;


import com.gestion.sgc.application.dto.request.ProductoRequest;
import com.gestion.sgc.application.dto.response.ProductoResponse;
import com.gestion.sgc.domain.aggregates.constans.EstadoProducto;
import com.gestion.sgc.domain.aggregates.model.Producto;
import com.gestion.sgc.domain.aggregates.model.TipoProducto;
import com.gestion.sgc.domain.ports.inputs.ProductosIn;
import com.gestion.sgc.domain.ports.outputs.ProductoRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.TipoProductoRepositoryPort;
import com.gestion.sgc.infraestructure.mapper.ProductoMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoUseCase implements ProductosIn {

    private final ProductoRepositoryPort productoRepository;
    private final TipoProductoRepositoryPort tipoProductoRepository;
    private final ProductoMapper productoMapper;

    @Override
    @Transactional
    public ProductoResponse createProducto(ProductoRequest request) {
        // Validar que no exista producto con mismo nombre
        if (productoRepository.existsByNombre(request.getNombre())) {
            throw new RuntimeException("Ya existe un producto con el nombre: " + request.getNombre());
        }

        // Validar que el tipo de producto exista
        TipoProducto tipoProducto = tipoProductoRepository.findById(request.getTipoProductoId())
                .orElseThrow(() -> new RuntimeException("Tipo de producto no encontrado con ID: " + request.getTipoProductoId()));

        // Crear producto usando el mapper
        Producto producto = productoMapper.toDomainFromRequest(request);
        producto.setTipoProducto(tipoProducto);

        // Si no se envió estado, por defecto ACTIVO
        if (request.getEstado() == null || request.getEstado().isEmpty()) {
            producto.setEstadoProducto(EstadoProducto.ACTIVO);  // ← Usando enum
        } else {
            // Convertir String a enum
            producto.setEstadoProducto(EstadoProducto.valueOf(request.getEstado()));
        }

        Producto productoCreado = productoRepository.save(producto);
        return productoMapper.toResponse(productoCreado);
    }

    @Override
    @Transactional
    public ProductoResponse UpdateProducto(Long id, ProductoRequest request) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        // Validar nombre único (excepto si es el mismo producto)
        if (!productoExistente.getNombre().equals(request.getNombre())
                && productoRepository.existsByNombre(request.getNombre())) {
            throw new RuntimeException("Ya existe otro producto con el nombre: " + request.getNombre());
        }

        // Validar tipo de producto
        TipoProducto tipoProducto = tipoProductoRepository.findById(request.getTipoProductoId())
                .orElseThrow(() -> new RuntimeException("Tipo de producto no encontrado con ID: " + request.getTipoProductoId()));

        // Actualizar datos
        productoExistente.setNombre(request.getNombre());
        productoExistente.setDescripcion(request.getDescripcion());
        productoExistente.setPrecioVenta(request.getPrecioVenta());
        productoExistente.setStockMinimo(request.getStockMinimo());  // ← int primitivo
        productoExistente.setTipoProducto(tipoProducto);

        // Actualizar estado si viene en el request
        if (request.getEstado() != null && !request.getEstado().isEmpty()) {
            productoExistente.setEstadoProducto(EstadoProducto.valueOf(request.getEstado()));
        }

        Producto productoActualizado = productoRepository.save(productoExistente);
        return productoMapper.toResponse(productoActualizado);
    }

    @Override
    public Optional<ProductoResponse> findById(Long id) {
        return productoRepository.findById(id)
                .map(productoMapper::toResponse);
    }

    @Override
    public List<ProductoResponse> getAllProductos() {
        return productoMapper.toResponseList(productoRepository.findAll());
    }

    @Override
    public List<ProductoResponse> getAllTipos(Long tipoProductoId) {
        return productoMapper.toResponseList(
                productoRepository.findByTipoProductoId(tipoProductoId)
        );
    }

    @Override
    public List<ProductoResponse> getAllStockMinimo() {
        // Asumiendo que stock_minimo es el umbral
        return productoMapper.toResponseList(
                productoRepository.findByStockBajo(10) // O el valor que consideres
        );
    }

    @Override
    @Transactional
    public void deleteProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, String estado) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        producto.setEstadoProducto(EstadoProducto.valueOf(estado));
        productoRepository.save(producto);
    }


}
