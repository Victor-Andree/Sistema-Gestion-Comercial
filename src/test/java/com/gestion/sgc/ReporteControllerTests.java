package com.gestion.sgc;

import com.gestion.sgc.application.dto.response.ReporteVentasResponse;
import com.gestion.sgc.application.services.ReporteExcelService;
import com.gestion.sgc.application.services.ReporteVentasService;
import com.gestion.sgc.infraestructure.controllers.ReporteController;
import com.gestion.sgc.infraestructure.security.config.SecurityConfig;
import com.gestion.sgc.infraestructure.security.filters.JwtTokenValidatorFilter;
import com.gestion.sgc.infraestructure.security.services.CustomAccessDeniendHandler;
import com.gestion.sgc.infraestructure.security.services.CustomAuthenticationEntryPoint;
import com.gestion.sgc.infraestructure.security.services.JwtService;
import com.gestion.sgc.infraestructure.security.services.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReporteController.class)
@Import({SecurityConfig.class, JwtTokenValidatorFilter.class, CustomAuthenticationEntryPoint.class,
        CustomAccessDeniendHandler.class})
class ReporteControllerTests {
    @Autowired MockMvc mvc;
    @MockitoBean ReporteVentasService reports;
    @MockitoBean ReporteExcelService excel;
    @MockitoBean UserDetailsService userDetails;
    @MockitoBean JwtService jwtService;
    @MockitoBean TokenBlacklistService blacklist;

    private static final String JSON = "/api/v1/reportes/ventas?desde=2026-10-01&hasta=2026-10-27";
    private static final String XLSX = "/api/v1/reportes/ventas.xlsx?desde=2026-10-01&hasta=2026-10-27";

    @Test void requiresAuthentication() throws Exception {
        mvc.perform(get(JSON)).andExpect(status().isUnauthorized());
        mvc.perform(get(XLSX)).andExpect(status().isUnauthorized());
    }

    @Test void assistantCannotReadOrDownloadReports() throws Exception {
        mvc.perform(get(JSON).with(user("assistant").roles("ASISTENTE"))).andExpect(status().isForbidden());
        mvc.perform(get(XLSX).with(user("assistant").roles("ASISTENTE"))).andExpect(status().isForbidden());
    }

    @Test void adminCanReadAndDownloadReports() throws Exception {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 27);
        when(reports.obtener(from, to)).thenReturn(new ReporteVentasResponse(from, to, 0,
                new BigDecimal("0.00"), new BigDecimal("0.00"), List.of(), List.of()));
        when(excel.exportar(from, to)).thenReturn(new byte[]{1, 2, 3});

        mvc.perform(get(JSON).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("application/json"));
        mvc.perform(get(XLSX).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(content().contentType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }
}
