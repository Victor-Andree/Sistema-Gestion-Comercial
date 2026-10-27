package com.gestion.sgc.application.services;

import com.gestion.sgc.application.dto.response.ReporteVentasResponse;
import com.gestion.sgc.infraestructure.entity.DetalleVentaEntity;
import com.gestion.sgc.infraestructure.entity.VentaEntity;
import com.gestion.sgc.infraestructure.repository.VentaJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
        double ingresos = 0;

        for (VentaEntity venta : ventas) {
            ingresos += venta.getTotal();
            ResumenDia dia = dias.get(venta.getFechaVenta().toLocalDate());
            dia.cantidad++;
            dia.ingresos += venta.getTotal();
            for (DetalleVentaEntity detalle : venta.getDetalles()) {
                Long productoId = detalle.getProducto().getProductoId();
                ResumenProducto producto = productos.computeIfAbsent(productoId,
                        ignored -> new ResumenProducto(detalle.getProducto().getNombre()));
                producto.unidades += detalle.getCantidad();
                producto.ingresos += detalle.getSubtotal();
            }
        }

        List<ReporteVentasResponse.PuntoVenta> evolucion = dias.entrySet().stream()
                .map(entry -> new ReporteVentasResponse.PuntoVenta(entry.getKey(), entry.getValue().cantidad,
                        redondear(entry.getValue().ingresos)))
                .toList();
        List<ReporteVentasResponse.ProductoVendido> ranking = productos.entrySet().stream()
                .map(entry -> new ReporteVentasResponse.ProductoVendido(entry.getKey(), entry.getValue().nombre,
                        entry.getValue().unidades, redondear(entry.getValue().ingresos)))
                .sorted(Comparator.comparingLong(ReporteVentasResponse.ProductoVendido::unidades).reversed())
                .toList();

        return new ReporteVentasResponse(desde, hasta, ventas.size(), redondear(ingresos),
                ventas.isEmpty() ? 0 : redondear(ingresos / ventas.size()), evolucion, ranking);
    }

    public String exportarCsv(LocalDate desde, LocalDate hasta) {
        ReporteVentasResponse reporte = obtener(desde, hasta);
        List<String> lineas = new ArrayList<>();
        lineas.add("fecha,cantidad_ventas,ingresos");
        reporte.evolucion().forEach(dia -> lineas.add(String.format(Locale.US, "%s,%d,%.2f",
                dia.fecha(), dia.cantidadVentas(), dia.ingresos())));
        lineas.add("");
        lineas.add("producto,unidades,ingresos");
        reporte.productosMasVendidos().forEach(producto -> lineas.add(String.format(Locale.US, "\"%s\",%d,%.2f",
                producto.nombre().replace("\"", "\"\""), producto.unidades(), producto.ingresos())));
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

    private double redondear(double valor) { return Math.round(valor * 100.0) / 100.0; }
    private static final class ResumenDia { long cantidad; double ingresos; }
    private static final class ResumenProducto {
        final String nombre; long unidades; double ingresos;
        ResumenProducto(String nombre) { this.nombre = nombre; }
    }
}
