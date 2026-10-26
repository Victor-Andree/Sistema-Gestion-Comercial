package com.gestion.sgc;

import com.gestion.sgc.application.dto.request.ProductoRequest;
import com.gestion.sgc.application.useCase.ProductoUseCase;
import com.gestion.sgc.application.useCase.StockUseCase;
import com.gestion.sgc.domain.aggregates.model.Producto;
import com.gestion.sgc.domain.aggregates.model.Stock;
import com.gestion.sgc.domain.aggregates.model.TipoProducto;
import com.gestion.sgc.domain.ports.outputs.*;
import com.gestion.sgc.infraestructure.mapper.ProductoMapper;
import com.gestion.sgc.infraestructure.mapper.StockMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryUseCaseTests {
    private final StockRepositoryPort stocks = mock(StockRepositoryPort.class);
    private final ProductoRepositoryPort products = mock(ProductoRepositoryPort.class);
    private final TipoProductoRepositoryPort categories = mock(TipoProductoRepositoryPort.class);
    private final ProductoMapper productMapper = mock(ProductoMapper.class);
    private final StockMapper stockMapper = mock(StockMapper.class);
    private final StockUseCase stockService = new StockUseCase(stocks, products, stockMapper);
    private final ProductoUseCase productService = new ProductoUseCase(products, categories, productMapper, stocks);
    private Stock stock;

    @BeforeEach void setup() {
        stock = new Stock(null, new Producto(), 10, null);
        when(stocks.findByProductoId(1L)).thenReturn(Optional.of(stock));
        when(stocks.save(any())).thenAnswer(call -> call.getArgument(0));
    }

    @ParameterizedTest @ValueSource(ints = {-1, 0})
    void rejectsNonPositiveMovementsAndChecks(int quantity) {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> stockService.updateStock(1L, quantity, "OUTCOME")).getStatusCode().value());
        assertThrows(ResponseStatusException.class, () -> stockService.updateStock(1L, quantity, "INCOME"));
        assertThrows(ResponseStatusException.class, () -> stockService.checkAvailability(1L, quantity));
        verify(stocks, never()).save(any());
    }

    @Test void rejectsNegativeAdjustment() {
        assertThrows(ResponseStatusException.class, () -> stockService.adjustStock(1L, -1, null));
        verify(stocks, never()).save(any());
    }

    @Test void permitsZeroPhysicalCount() {
        stockService.adjustStock(1L, 0, null);
        assertEquals(0, stock.getCantidadActual());
        verify(stocks).save(stock);
    }

    @Test void refusesInsufficientStock() {
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> stockService.updateStock(1L, 11, "OUTCOME")).getStatusCode().value());
        assertEquals(10, stock.getCantidadActual());
        verify(stocks, never()).save(any());
    }

    @Test void movementUpdatesStock() {
        stockService.updateStock(1L, 3, "OUTCOME");
        assertEquals(7, stock.getCantidadActual());
        stockService.updateStock(1L, 5, "INCOME");
        assertEquals(12, stock.getCantidadActual());
    }

    @Test void refusesIntegerOverflow() {
        stock.setCantidadActual(Integer.MAX_VALUE);
        assertThrows(ResponseStatusException.class, () -> stockService.updateStock(1L, 1, "INCOME"));
        verify(stocks, never()).save(any());
    }

    @Test void refusesUnknownMovement() {
        assertThrows(ResponseStatusException.class, () -> stockService.updateStock(1L, 2, "OTHER"));
        verify(stocks, never()).save(any());
    }

    private ProductoRequest creation(Integer initial) {
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Filtro"); request.setTipoProductoId(2L); request.setCantidadInicial(initial);
        when(categories.findById(2L)).thenReturn(Optional.of(new TipoProducto(2L, "Filtros")));
        when(productMapper.toDomainFromRequest(request)).thenReturn(new Producto());
        when(products.save(any())).thenAnswer(call -> call.getArgument(0));
        return request;
    }

    @Test void createsInitialStockWhenRequested() {
        productService.createProducto(creation(15));
        verify(stocks).save(argThat(value -> value.getCantidadActual() == 15 && value.getProducto() != null));
    }

    @Test void preservesLegacyCreationWithoutStock() {
        productService.createProducto(creation(null));
        verify(stocks, never()).save(any());
    }

    @Test void refusesNegativeInitialStockBeforeSavingProduct() {
        assertThrows(ResponseStatusException.class, () -> productService.createProducto(creation(-1)));
        verify(products, never()).save(any());
    }

    @Test void rejectsDuplicateProduct() {
        ProductoRequest request = creation(0);
        when(products.existsByNombre("Filtro")).thenReturn(true);
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> productService.createProducto(request)).getStatusCode().value());
        verify(products, never()).save(any());
    }

    @Test void lowStockUsesActualInventory() {
        when(stocks.findByStockBajo()).thenReturn(List.of(stock));
        productService.getAllStockMinimo();
        verify(productMapper).toResponseList(List.of(stock.getProducto()));
        verify(products, never()).findByStockBajo(any());
    }

    @Test void invalidStateReturnsBadRequest() {
        when(products.findById(1L)).thenReturn(Optional.of(new Producto()));
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> productService.cambiarEstado(1L, "INVALIDO")).getStatusCode().value());
        verify(products, never()).save(any());
    }
}
