package com.gestion.sgc;

import com.gestion.sgc.application.dto.response.ReporteVentasResponse;
import com.gestion.sgc.application.services.ReporteVentasService;
import com.gestion.sgc.infraestructure.entity.DetalleVentaEntity;
import com.gestion.sgc.infraestructure.entity.ProductoEntity;
import com.gestion.sgc.infraestructure.entity.VentaEntity;
import com.gestion.sgc.infraestructure.repository.VentaJpaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReporteVentasServiceTests {
    private final VentaJpaRepository repository = mock(VentaJpaRepository.class);
    private final ReporteVentasService service = new ReporteVentasService(repository);

    @Test
    void aggregatesCompletedSalesByDayAndProduct() {
        LocalDate desde = LocalDate.of(2026, 10, 1);
        LocalDate hasta = LocalDate.of(2026, 10, 2);
        ProductoEntity producto = new ProductoEntity();
        producto.setProductoId(7L);
        producto.setNombre("Filtro");
        DetalleVentaEntity detalle = new DetalleVentaEntity();
        detalle.setProducto(producto);
        detalle.setCantidad(2);
        detalle.setSubtotal(50.0);
        VentaEntity venta = new VentaEntity();
        venta.setFechaVenta(LocalDateTime.of(2026, 10, 1, 12, 0));
        venta.setTotal(50.0);
        venta.setDetalles(List.of(detalle));
        when(repository.findCompletedForReport(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(venta));

        ReporteVentasResponse result = service.obtener(desde, hasta);

        assertEquals(1, result.cantidadVentas());
        assertEquals(new BigDecimal("50.00"), result.ingresosTotales());
        assertEquals(2, result.evolucion().size());
        assertEquals(2, result.productosMasVendidos().getFirst().unidades());
    }

    @Test
    void rejectsInvalidRange() {
        assertThrows(IllegalArgumentException.class,
                () -> service.obtener(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 1)));
    }

    @Test
    void returnsZeroSummaryForEmptyPeriod() {
        LocalDate fecha = LocalDate.of(2026, 10, 5);
        when(repository.findCompletedForReport(fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay()))
                .thenReturn(List.of());

        ReporteVentasResponse result = service.obtener(fecha, fecha);

        assertEquals(0, result.cantidadVentas());
        assertEquals(new BigDecimal("0.00"), result.ingresosTotales());
        assertEquals(1, result.evolucion().size());
    }

    @Test
    void csvUsesExcelFriendlySeparatorAndNeutralizesFormulas() {
        LocalDate fecha = LocalDate.of(2026, 10, 5);
        ProductoEntity producto = new ProductoEntity();
        producto.setProductoId(9L);
        producto.setNombre("=FORMULA");
        DetalleVentaEntity detalle = new DetalleVentaEntity();
        detalle.setProducto(producto);
        detalle.setCantidad(1);
        detalle.setSubtotal(10.0);
        VentaEntity venta = new VentaEntity();
        venta.setFechaVenta(fecha.atTime(10, 0));
        venta.setTotal(10.0);
        venta.setDetalles(List.of(detalle));
        when(repository.findCompletedForReport(fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(venta));

        String csv = service.exportarCsv(fecha, fecha);

        assertTrue(csv.contains("Fecha;Ventas completadas;Ingresos (S/)"));
        assertTrue(csv.contains("\"'=FORMULA\";1;10,00"));
    }
}
