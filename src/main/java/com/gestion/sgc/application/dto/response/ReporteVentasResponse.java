package com.gestion.sgc.application.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ReporteVentasResponse(
        LocalDate desde,
        LocalDate hasta,
        long cantidadVentas,
        double ingresosTotales,
        double ticketPromedio,
        List<PuntoVenta> evolucion,
        List<ProductoVendido> productosMasVendidos
) {
    public record PuntoVenta(LocalDate fecha, long cantidadVentas, double ingresos) {}
    public record ProductoVendido(Long productoId, String nombre, long unidades, double ingresos) {}
}
