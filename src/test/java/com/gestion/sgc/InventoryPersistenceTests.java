package com.gestion.sgc;

import com.gestion.sgc.application.dto.request.ProductoRequest;
import com.gestion.sgc.application.useCase.ProductoUseCase;
import com.gestion.sgc.domain.ports.outputs.StockRepositoryPort;
import com.gestion.sgc.infraestructure.adapters.*;
import com.gestion.sgc.infraestructure.entity.TipoProductoEntity;
import com.gestion.sgc.infraestructure.mapper.*;
import com.gestion.sgc.infraestructure.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@DataJpaTest(bootstrapMode = org.springframework.data.repository.config.BootstrapMode.LAZY,
        properties = {"spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect", "logging.level.org.hibernate.SQL=WARN"})
@Import({ProductoUseCase.class, ProductoRepositoryAdapter.class, TipoProductoRepositoryAdapter.class,
        StockRepositoryAdapter.class, ProductoMapperImpl.class, TipoProductoMapperImpl.class, StockMapperImpl.class})
class InventoryPersistenceTests {
    @Autowired ProductoUseCase service;
    @Autowired ProductoJpaRepository products;
    @Autowired TipoProductoJpaRepository categories;
    @Autowired StockJpaRepository stocks;
    @Autowired PlatformTransactionManager transactions;
    @MockitoSpyBean StockRepositoryPort stockPort;

    private Long category() {
        TipoProductoEntity category = new TipoProductoEntity();
        category.setNombre("Filtros");
        return categories.saveAndFlush(category).getTipoProductoId();
    }

    private ProductoRequest request(String name, Long category) {
        ProductoRequest request = new ProductoRequest();
        request.setNombre(name); request.setDescripcion("Descripción compartida");
        request.setPrecioVenta(10.0); request.setStockMinimo(5); request.setTipoProductoId(category);
        request.setCantidadInicial(3); request.setEstado("ACTIVO");
        return request;
    }

    @Test void productsCanSharePriceDescriptionAndMinimumAndHaveInitialStock() {
        Long category = category();
        var first = service.createProducto(request("Filtro uno", category));
        var second = service.createProducto(request("Filtro dos", category));
        products.flush();
        assertNotEquals(first.getProductoId(), second.getProductoId());
        assertEquals(2, products.count());
        assertEquals(3, stocks.findByProductoProductoId(first.getProductoId()).orElseThrow().getCantidadActual());
        assertEquals(2, service.getAllStockMinimo().size());
    }

    @Test void editingDoesNotOverwriteExistingStock() {
        Long category = category();
        var created = service.createProducto(request("Filtro", category));
        var update = request("Filtro editado", category);
        update.setCantidadInicial(null); update.setEstado("INACTIVO");
        service.UpdateProducto(created.getProductoId(), update);
        products.flush();
        assertEquals(3, stocks.findByProductoProductoId(created.getProductoId()).orElseThrow().getCantidadActual());
        assertEquals(com.gestion.sgc.domain.aggregates.constans.EstadoProducto.INACTIVO,
                service.findById(created.getProductoId()).orElseThrow().getEstadoProducto());
    }

    @Test @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void stockFailureRollsBackProductCreation() {
        TransactionTemplate transaction = new TransactionTemplate(transactions);
        Long category = transaction.execute(status -> category());
        long before = products.count();
        doThrow(new IllegalStateException("Fallo simulado de stock")).when(stockPort).save(any());
        try {
            assertThrows(IllegalStateException.class, () -> service.createProducto(request("No debe persistir", category)));
            assertEquals(before, products.count());
            assertFalse(products.existsByNombre("No debe persistir"));
        } finally {
            transaction.executeWithoutResult(status -> categories.deleteById(category));
        }
    }
}
