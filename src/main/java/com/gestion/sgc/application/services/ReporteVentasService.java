package com.gestion.sgc.application.services;

import com.gestion.sgc.application.dto.response.ReporteVentasResponse;
import com.gestion.sgc.infraestructure.entity.DetalleVentaEntity;
import com.gestion.sgc.infraestructure.entity.VentaEntity;
import com.gestion.sgc.infraestructure.repository.VentaJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteVentasService {
    private final VentaJpaRepository ventaRepository;

    public ReporteVentasService(VentaJpaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    @Transactional(readOnly = true)
    public ReporteVentasResponse obtener(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        List<VentaEntity> ventas = ventaRepository.findCompletedForReport(
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());

        Map<LocalDate, ResumenDia> dias = new LinkedHashMap<>();
        desde.datesUntil(hasta.plusDays(1)).forEach(fecha -> dias.put(fecha, new ResumenDia()));
        Map<Long, ResumenProducto> productos = new LinkedHashMap<>();
        BigDecimal ingresos = BigDecimal.ZERO;

        for (VentaEntity venta : ventas) {
            ingresos = ingresos.add(BigDecimal.valueOf(venta.getTotal()));
            ResumenDia dia = dias.get(venta.getFechaVenta().toLocalDate());
            dia.cantidad++;
            dia.ingresos = dia.ingresos.add(BigDecimal.valueOf(venta.getTotal()));
            for (DetalleVentaEntity detalle : venta.getDetalles()) {
                Long productoId = detalle.getProducto().getProductoId();
                ResumenProducto producto = productos.computeIfAbsent(productoId,
                        ignored -> new ResumenProducto(detalle.getProducto().getNombre()));
                producto.unidades += detalle.getCantidad();
                producto.ingresos = producto.ingresos.add(BigDecimal.valueOf(detalle.getSubtotal()));
            }
        }

        List<ReporteVentasResponse.PuntoVenta> evolucion = dias.entrySet().stream()
                .map(entry -> new ReporteVentasResponse.PuntoVenta(entry.getKey(), entry.getValue().cantidad,
                        dinero(entry.getValue().ingresos)))
                .toList();
        List<ReporteVentasResponse.ProductoVendido> ranking = productos.entrySet().stream()
                .map(entry -> new ReporteVentasResponse.ProductoVendido(entry.getKey(), entry.getValue().nombre,
                        entry.getValue().unidades, dinero(entry.getValue().ingresos)))
                .sorted(Comparator.comparingLong(ReporteVentasResponse.ProductoVendido::unidades).reversed())
                .toList();

        return new ReporteVentasResponse(desde, hasta, ventas.size(), dinero(ingresos),
                ventas.isEmpty() ? BigDecimal.ZERO.setScale(2) : dinero(ingresos.divide(
                        BigDecimal.valueOf(ventas.size()), 4, RoundingMode.HALF_UP)), evolucion, ranking);
    }

    public String exportarCsv(LocalDate desde, LocalDate hasta) {
        ReporteVentasResponse reporte = obtener(desde, hasta);
        List<String> lineas = new ArrayList<>();
        String periodo = tipoPeriodo(desde, hasta);
        lineas.add("REPORTE COMERCIAL DE VENTAS");
        lineas.add("Período;" + periodo);
        lineas.add("Desde;" + fechaCsv(desde));
        lineas.add("Hasta;" + fechaCsv(hasta));
        lineas.add("Ventas completadas;" + reporte.cantidadVentas());
        lineas.add("Ingresos totales (S/);" + numeroCsv(reporte.ingresosTotales()));
        lineas.add("Ticket promedio (S/);" + numeroCsv(reporte.ticketPromedio()));
        lineas.add("");
        lineas.add("DETALLE DIARIO");
        lineas.add("Fecha;Ventas completadas;Ingresos (S/)");
        reporte.evolucion().forEach(dia -> lineas.add(String.format("%s;%d;%s",
                fechaCsv(dia.fecha()), dia.cantidadVentas(), numeroCsv(dia.ingresos()))));
        lineas.add("");
        lineas.add("PRODUCTOS MÁS VENDIDOS");
        lineas.add("Producto;Unidades;Ingresos (S/)");
        reporte.productosMasVendidos().forEach(producto -> lineas.add(String.format("%s;%d;%s",
                celdaCsv(producto.nombre()), producto.unidades(), numeroCsv(producto.ingresos()))));
        return "\uFEFF" + String.join("\r\n", lineas);
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || hasta.isBefore(desde)) {
            throw new IllegalArgumentException("El rango de fechas no es válido");
        }
        if (desde.plusYears(2).isBefore(hasta)) {
            throw new IllegalArgumentException("El rango máximo permitido es de dos años");
        }
    }

    private BigDecimal dinero(BigDecimal valor) { return valor.setScale(2, RoundingMode.HALF_UP); }
    private String numeroCsv(BigDecimal valor) { return dinero(valor).toPlainString().replace('.', ','); }
    private String fechaCsv(LocalDate fecha) { return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
    private String tipoPeriodo(LocalDate desde, LocalDate hasta) {
        long dias = java.time.temporal.ChronoUnit.DAYS.between(desde, hasta) + 1;
        if (dias == 1) return "Día";
        if (dias == 7) return "Semana";
        if (desde.getDayOfMonth() == 1 && desde.getMonth().equals(hasta.getMonth())
                && desde.getYear() == hasta.getYear()) {
            return hasta.equals(desde.withDayOfMonth(desde.lengthOfMonth())) ? "Mes" : "Mes en curso";
        }
        return "Rango personalizado (" + dias + " días)";
    }
    private String celdaCsv(String valor) {
        String seguro = valor == null ? "" : valor;
        if (!seguro.isEmpty() && "=+-@\t\r".indexOf(seguro.charAt(0)) >= 0) seguro = "'" + seguro;
        return "\"" + seguro.replace("\"", "\"\"") + "\"";
    }
    private static final class ResumenDia { long cantidad; BigDecimal ingresos = BigDecimal.ZERO; }
    private static final class ResumenProducto {
        final String nombre; long unidades; BigDecimal ingresos = BigDecimal.ZERO;
        ResumenProducto(String nombre) { this.nombre = nombre; }
    }
}
