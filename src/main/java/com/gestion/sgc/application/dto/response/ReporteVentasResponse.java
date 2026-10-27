package com.gestion.sgc.application.dto.response;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

public record ReporteVentasResponse(
        LocalDate desde,
        LocalDate hasta,
        long cantidadVentas,
        BigDecimal ingresosTotales,
        BigDecimal ticketPromedio,
        List<PuntoVenta> evolucion,
        List<ProductoVendido> productosMasVendidos
) {
    public record PuntoVenta(LocalDate fecha, long cantidadVentas, BigDecimal ingresos) {}
    public record ProductoVendido(Long productoId, String nombre, long unidades, BigDecimal ingresos) {}
}
