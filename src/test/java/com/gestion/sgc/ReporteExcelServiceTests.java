package com.gestion.sgc;

import com.gestion.sgc.application.dto.response.ReporteVentasResponse;
import com.gestion.sgc.application.services.ReporteExcelService;
import com.gestion.sgc.application.services.ReporteVentasService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReporteExcelServiceTests {
    @Test
    void createsStyledWorkbookWithSummaryAndDailyDetail() throws Exception {
        LocalDate fecha = LocalDate.of(2026, 10, 27);
        ReporteVentasService reportes = mock(ReporteVentasService.class);
        ReporteVentasResponse reporte = new ReporteVentasResponse(fecha, fecha, 2,
                new BigDecimal("150.00"), new BigDecimal("75.00"),
                List.of(new ReporteVentasResponse.PuntoVenta(fecha, 2, new BigDecimal("150.00"))),
                List.of(new ReporteVentasResponse.ProductoVendido(1L, "Filtro", 3, new BigDecimal("150.00"))));
        when(reportes.obtener(fecha, fecha)).thenReturn(reporte);

        byte[] bytes = new ReporteExcelService(reportes).exportar(fecha, fecha);

        assertTrue(bytes.length > 0);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(2, workbook.getNumberOfSheets());
            assertEquals("REPORTE COMERCIAL DE VENTAS", workbook.getSheet("Resumen").getRow(0).getCell(0).getStringCellValue());
            assertEquals(150.0, workbook.getSheet("Resumen").getRow(5).getCell(1).getNumericCellValue());
            assertEquals("Fecha", workbook.getSheet("Detalle diario").getRow(0).getCell(0).getStringCellValue());
        }
    }
}
