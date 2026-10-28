package com.gestion.sgc;

import com.gestion.sgc.domain.ports.inputs.MovimientoCajaIn;
import com.gestion.sgc.infraestructure.controllers.MovimientoCajaController;
import com.gestion.sgc.infraestructure.exception.InventoryExceptionHandler;
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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovimientoCajaController.class)
@Import({SecurityConfig.class, JwtTokenValidatorFilter.class, CustomAuthenticationEntryPoint.class,
        CustomAccessDeniendHandler.class, InventoryExceptionHandler.class})
class MovimientoCajaControllerTests {
    @Autowired MockMvc mvc;
    @MockitoBean MovimientoCajaIn movements;
    @MockitoBean UserDetailsService userDetails;
    @MockitoBean JwtService jwtService;
    @MockitoBean TokenBlacklistService blacklist;

    @Test void anonymousGets401() throws Exception {
        mvc.perform(get("/api/v1/cajas/sesiones/1/movimientos")).andExpect(status().isUnauthorized());
    }

    @Test void assistantCanReadButCannotWrite() throws Exception {
        mvc.perform(get("/api/v1/cajas/sesiones/1/movimientos").with(user("assistant").roles("ASISTENTE")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/cajas/sesiones/1/resumen").with(user("assistant").roles("ASISTENTE")))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/cajas/sesiones/1/movimientos").with(user("assistant").roles("ASISTENTE"))
                        .contentType("application/json")
                        .content("{\"tipo\":\"INGRESO\",\"monto\":10,\"concepto\":\"Fondo\"}"))
                .andExpect(status().isForbidden());
    }

    @Test void adminCanWriteAndUsernameComesFromAuthentication() throws Exception {
        mvc.perform(post("/api/v1/cajas/sesiones/1/movimientos").with(user("admin-real").roles("ADMIN"))
                        .contentType("application/json")
                        .content("{\"tipo\":\"EGRESO\",\"monto\":10.50,\"concepto\":\"Compra\"}"))
                .andExpect(status().isCreated());
        verify(movements).registrar(org.mockito.ArgumentMatchers.eq(1L),
                argThat(request -> request.monto().equals(new BigDecimal("10.50")) && request.concepto().equals("Compra")),
                org.mockito.ArgumentMatchers.eq("admin-real"));
    }

    @Test void invalidBodyReturns400WithoutUseCase() throws Exception {
        mvc.perform(post("/api/v1/cajas/sesiones/1/movimientos").with(user("admin").roles("ADMIN"))
                        .contentType("application/json")
                        .content("{\"tipo\":\"INGRESO\",\"monto\":0,\"concepto\":\"\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(movements);
    }
}
