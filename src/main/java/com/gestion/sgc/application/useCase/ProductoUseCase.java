package com.gestion.sgc.application.useCase;


import com.gestion.sgc.application.dto.request.ProductoRequest;
import com.gestion.sgc.application.dto.response.ProductoResponse;
import com.gestion.sgc.domain.aggregates.constans.EstadoProducto;
import com.gestion.sgc.domain.aggregates.model.Producto;
import com.gestion.sgc.domain.aggregates.model.TipoProducto;
import com.gestion.sgc.domain.aggregates.model.Stock;
import com.gestion.sgc.domain.ports.outputs.StockRepositoryPort;
import com.gestion.sgc.domain.ports.inputs.ProductosIn;
import com.gestion.sgc.domain.ports.outputs.ProductoRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.TipoProductoRepositoryPort;
import com.gestion.sgc.infraestructure.mapper.ProductoMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductoUseCase implements ProductosIn {

    private final ProductoRepositoryPort productoRepository;
    private final TipoProductoRepositoryPort tipoProductoRepository;
    private final ProductoMapper productoMapper;
    private final StockRepositoryPort stockRepository;

    @Override
    @Transactional
    public ProductoResponse createProducto(ProductoRequest request) {
        if (request.getCantidadInicial() != null && request.getCantidadInicial() < 0) {
            throw new ResponseStatusException(BAD_REQUEST, "El stock inicial no puede ser negativo");
        }
        // Validar que no exista producto con mismo nombre
        if (productoRepository.existsByNombre(request.getNombre())) {
            throw new ResponseStatusException(CONFLICT, "Ya existe un producto con el nombre: " + request.getNombre());
        }

        // Validar que el tipo de producto exista
        TipoProducto tipoProducto = tipoProductoRepository.findById(request.getTipoProductoId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tipo de producto no encontrado con ID: " + request.getTipoProductoId()));

        // Crear producto usando el mapper
        Producto producto = productoMapper.toDomainFromRequest(request);
        producto.setTipoProducto(tipoProducto);

        // Si no se envió estado, por defecto ACTIVO
        if (request.getEstado() == null || request.getEstado().isEmpty()) {
            producto.setEstadoProducto(EstadoProducto.ACTIVO);  // ← Usando enum
        } else {
            // Convertir String a enum
            producto.setEstadoProducto(parseEstado(request.getEstado()));
        }

        Producto productoCreado = productoRepository.save(producto);
        if (request.getCantidadInicial() != null) {
            stockRepository.save(new Stock(null, productoCreado, request.getCantidadInicial(), LocalDateTime.now()));
        }
        return productoMapper.toResponse(productoCreado);
    }

    @Override
    @Transactional
    public ProductoResponse UpdateProducto(Long id, ProductoRequest request) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Producto no encontrado con ID: " + id));

        // Validar nombre único (excepto si es el mismo producto)
        if (!productoExistente.getNombre().equals(request.getNombre())
                && productoRepository.existsByNombre(request.getNombre())) {
            throw new ResponseStatusException(CONFLICT, "Ya existe otro producto con el nombre: " + request.getNombre());
        }

        // Validar tipo de producto
        TipoProducto tipoProducto = tipoProductoRepository.findById(request.getTipoProductoId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tipo de producto no encontrado con ID: " + request.getTipoProductoId()));

        // Actualizar datos
        productoExistente.setNombre(request.getNombre());
        productoExistente.setDescripcion(request.getDescripcion());
        productoExistente.setPrecioVenta(request.getPrecioVenta());
        productoExistente.setStockMinimo(request.getStockMinimo());  // ← int primitivo
        productoExistente.setTipoProducto(tipoProducto);

        // Actualizar estado si viene en el request
        if (request.getEstado() != null && !request.getEstado().isEmpty()) {
            productoExistente.setEstadoProducto(parseEstado(request.getEstado()));
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
        return productoMapper.toResponseList(
                stockRepository.findByStockBajo().stream()
                        .map(Stock::getProducto).toList()
        );
    }

    @Override
    @Transactional
    public void deleteProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, String estado) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Producto no encontrado con ID: " + id));

        producto.setEstadoProducto(parseEstado(estado));
        productoRepository.save(producto);
    }


    private EstadoProducto parseEstado(String estado) {
        try {
            return EstadoProducto.valueOf(estado);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ResponseStatusException(BAD_REQUEST, "El estado debe ser ACTIVO o INACTIVO");
        }
    }
}
