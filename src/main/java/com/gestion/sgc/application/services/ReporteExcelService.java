package com.gestion.sgc.application.services;

import com.gestion.sgc.application.dto.response.ReporteVentasResponse;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ReporteExcelService {
    private final ReporteVentasService reporteService;

    public ReporteExcelService(ReporteVentasService reporteService) {
        this.reporteService = reporteService;
    }

    public byte[] exportar(LocalDate desde, LocalDate hasta) {
        ReporteVentasResponse reporte = reporteService.obtener(desde, hasta);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(workbook);
            crearResumen(workbook, reporte, estilos);
            crearDetalle(workbook, reporte, estilos);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo generar el archivo Excel", exception);
        }
    }

    private void crearResumen(XSSFWorkbook workbook, ReporteVentasResponse reporte, Estilos estilos) {
        Sheet sheet = workbook.createSheet("Resumen");
        sheet.createFreezePane(0, 1);
        Row title = sheet.createRow(0);
        title.setHeightInPoints(30);
        title.createCell(0).setCellValue("REPORTE COMERCIAL DE VENTAS");
        title.getCell(0).setCellStyle(estilos.titulo);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));

        filaResumen(sheet, 2, "Desde", reporte.desde(), estilos.fecha, estilos);
        filaResumen(sheet, 3, "Hasta", reporte.hasta(), estilos.fecha, estilos);
        filaResumen(sheet, 4, "Ventas completadas", reporte.cantidadVentas(), estilos.entero, estilos);
        filaResumen(sheet, 5, "Ingresos totales", reporte.ingresosTotales(), estilos.moneda, estilos);
        filaResumen(sheet, 6, "Ticket promedio", reporte.ticketPromedio(), estilos.moneda, estilos);

        Row section = sheet.createRow(8);
        section.createCell(0).setCellValue("PRODUCTOS MÁS VENDIDOS");
        section.getCell(0).setCellStyle(estilos.seccion);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(8, 8, 0, 3));
        Row headers = sheet.createRow(9);
        String[] labels = {"Puesto", "Producto", "Unidades", "Ingresos"};
        for (int column = 0; column < labels.length; column++) {
            headers.createCell(column).setCellValue(labels[column]);
            headers.getCell(column).setCellStyle(estilos.encabezado);
        }
        int rowIndex = 10;
        int rank = 1;
        for (ReporteVentasResponse.ProductoVendido producto : reporte.productosMasVendidos()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(rank++);
            row.createCell(1).setCellValue(producto.nombre());
            row.createCell(2).setCellValue(producto.unidades());
            row.createCell(3).setCellValue(producto.ingresos().doubleValue());
            row.getCell(3).setCellStyle(estilos.moneda);
        }
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(9, Math.max(9, rowIndex - 1), 0, 3));
        ajustar(sheet, 14, 38, 14, 18);
    }

    private void crearDetalle(XSSFWorkbook workbook, ReporteVentasResponse reporte, Estilos estilos) {
        Sheet sheet = workbook.createSheet("Detalle diario");
        sheet.createFreezePane(0, 1);
        Row headers = sheet.createRow(0);
        String[] labels = {"Fecha", "Ventas completadas", "Ingresos"};
        for (int column = 0; column < labels.length; column++) {
            headers.createCell(column).setCellValue(labels[column]);
            headers.getCell(column).setCellStyle(estilos.encabezado);
        }
        int rowIndex = 1;
        for (ReporteVentasResponse.PuntoVenta punto : reporte.evolucion()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(java.sql.Date.valueOf(punto.fecha()));
            row.getCell(0).setCellStyle(estilos.fecha);
            row.createCell(1).setCellValue(punto.cantidadVentas());
            row.createCell(2).setCellValue(punto.ingresos().doubleValue());
            row.getCell(2).setCellStyle(estilos.moneda);
        }
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, Math.max(0, rowIndex - 1), 0, 2));
        ajustar(sheet, 16, 22, 18);
    }

    private void filaResumen(Sheet sheet, int index, String label, Object value, CellStyle valueStyle, Estilos estilos) {
        Row row = sheet.createRow(index);
        row.createCell(0).setCellValue(label);
        row.getCell(0).setCellStyle(estilos.etiqueta);
        if (value instanceof LocalDate date) row.createCell(1).setCellValue(java.sql.Date.valueOf(date));
        else if (value instanceof BigDecimal decimal) row.createCell(1).setCellValue(decimal.doubleValue());
        else if (value instanceof Number number) row.createCell(1).setCellValue(number.doubleValue());
        else row.createCell(1).setCellValue(String.valueOf(value));
        row.getCell(1).setCellStyle(valueStyle);
    }

    private void ajustar(Sheet sheet, int... widths) {
        for (int column = 0; column < widths.length; column++) sheet.setColumnWidth(column, widths[column] * 256);
    }

    private static final class Estilos {
        final CellStyle titulo;
        final CellStyle seccion;
        final CellStyle encabezado;
        final CellStyle etiqueta;
        final CellStyle fecha;
        final CellStyle moneda;
        final CellStyle entero;

        Estilos(XSSFWorkbook workbook) {
            titulo = workbook.createCellStyle();
            titulo.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            titulo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titulo.setAlignment(HorizontalAlignment.CENTER);
            titulo.setVerticalAlignment(VerticalAlignment.CENTER);
            Font titleFont = workbook.createFont();
            titleFont.setBold(true); titleFont.setFontHeightInPoints((short) 16); titleFont.setColor(IndexedColors.WHITE.getIndex());
            titulo.setFont(titleFont);

            seccion = workbook.createCellStyle();
            seccion.cloneStyleFrom(titulo);
            seccion.setAlignment(HorizontalAlignment.LEFT);

            encabezado = workbook.createCellStyle();
            encabezado.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            encabezado.setBorderBottom(BorderStyle.THIN);
            Font headerFont = workbook.createFont(); headerFont.setBold(true); encabezado.setFont(headerFont);

            etiqueta = workbook.createCellStyle();
            Font labelFont = workbook.createFont(); labelFont.setBold(true); etiqueta.setFont(labelFont);
            fecha = workbook.createCellStyle(); fecha.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
            moneda = workbook.createCellStyle(); moneda.setDataFormat(workbook.createDataFormat().getFormat("\"S/ \"#,##0.00"));
            entero = workbook.createCellStyle(); entero.setDataFormat(workbook.createDataFormat().getFormat("0"));
        }
    }
}
