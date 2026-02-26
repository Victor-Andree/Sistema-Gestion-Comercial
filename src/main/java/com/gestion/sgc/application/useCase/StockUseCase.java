package com.gestion.sgc.application.useCase;


import com.gestion.sgc.application.dto.request.StockRequest;
import com.gestion.sgc.application.dto.response.StockResponse;
import com.gestion.sgc.domain.aggregates.model.Producto;
import com.gestion.sgc.domain.aggregates.model.Stock;
import com.gestion.sgc.domain.ports.inputs.StockIn;
import com.gestion.sgc.domain.ports.outputs.ProductoRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.StockRepositoryPort;
import com.gestion.sgc.infraestructure.mapper.StockMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockUseCase implements StockIn {

    private final StockRepositoryPort stockRepository;
    private final ProductoRepositoryPort productoRepository;
    private final StockMapper stockMapper;

    @Override
    @Transactional
    public StockResponse createInitialStock(StockRequest request) {
        // Verificar que el producto existe
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + request.getProductoId()));

        // Verificar que el producto no tenga stock ya asignado
        if (stockRepository.existsByProductoId(request.getProductoId())) {
            throw new RuntimeException("El producto ya tiene un registro de stock");
        }

        // Crear stock
        Stock stock = stockMapper.toDomainFromRequest(request);
        stock.setProducto(producto);
        stock.setCantidadActual(request.getCantidadInicial());
        stock.setFechaActualizacion(LocalDateTime.now());

        Stock stockGuardado = stockRepository.save(stock);
        log.info("Stock inicial creado para producto: {} con cantidad: {}",
                producto.getNombre(), request.getCantidadInicial());

        return stockMapper.toResponse(stockGuardado);
    }

    @Override
    public Optional<StockResponse> getStockByProductId(Long productId) {
        return stockRepository.findByProductoId(productId)
                .map(stock -> {
                    StockResponse response = stockMapper.toResponse(stock);
                    response.setEstadoStock(calcularEstadoStock(stock));
                    return response;
                });
    }

    @Override
    public List<StockResponse> listAllStocks() {
        return stockRepository.findAll().stream()
                .map(stock -> {
                    StockResponse response = stockMapper.toResponse(stock);
                    response.setEstadoStock(calcularEstadoStock(stock));
                    return response;
                })
                .toList();
    }

    @Override
    public List<StockResponse> listLowStock() {
        return stockRepository.findByStockBajo().stream()
                .map(stockMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public StockResponse updateStock(Long productId, Integer quantity, String type) {
        // Buscar stock
        Stock stock = stockRepository.findByProductoId(productId)
                .orElseThrow(() -> new RuntimeException("Stock no encontrado para producto ID: " + productId));

        int cantidadAnterior = stock.getCantidadActual();
        int cantidadNueva;

        // Procesar según tipo de movimiento
        if ("INCOME".equalsIgnoreCase(type)) {
            cantidadNueva = cantidadAnterior + quantity;
            log.info("INGRESO de stock: +{} unidades para producto ID: {}", quantity, productId);
        } else if ("OUTCOME".equalsIgnoreCase(type)) {
            if (cantidadAnterior < quantity) {
                throw new RuntimeException("Stock insuficiente. Disponible: " + cantidadAnterior);
            }
            cantidadNueva = cantidadAnterior - quantity;
            log.info("SALIDA de stock: -{} unidades para producto ID: {}", quantity, productId);
        } else {
            throw new RuntimeException("Tipo de movimiento inválido: " + type);
        }

        // Actualizar stock
        stock.setCantidadActual(cantidadNueva);
        stock.setFechaActualizacion(LocalDateTime.now());

        Stock stockActualizado = stockRepository.save(stock);
        return stockMapper.toResponse(stockActualizado);
    }

    @Override
    @Transactional
    public StockResponse adjustStock(Long productId, Integer realQuantity, String observation) {
        // Buscar stock
        Stock stock = stockRepository.findByProductoId(productId)
                .orElseThrow(() -> new RuntimeException("Stock no encontrado para producto ID: " + productId));

        int cantidadAnterior = stock.getCantidadActual();

        // Ajustar stock
        stock.setCantidadActual(realQuantity);
        stock.setFechaActualizacion(LocalDateTime.now());

        Stock stockAjustado = stockRepository.save(stock);

        log.info("Stock ajustado para producto ID: {} de {} a {}. Observación: {}",
                productId, cantidadAnterior, realQuantity, observation);

        return stockMapper.toResponse(stockAjustado);
    }

    @Override
    public boolean checkAvailability(Long productId, Integer requiredQuantity) {
        return stockRepository.findByProductoId(productId)
                .map(stock -> stock.getCantidadActual() >= requiredQuantity)
                .orElse(false);
    }

    // Métodos privados de ayuda
    private String calcularEstadoStock(Stock stock) {
        if (stock.getCantidadActual() <= 0) return "AGOTADO";
        if (stock.getCantidadActual() <= stock.getProducto().getStockMinimo() * 0.3) return "CRITICO";
        if (stock.getCantidadActual() <= stock.getProducto().getStockMinimo()) return "BAJO";
        return "NORMAL";
    }


}
