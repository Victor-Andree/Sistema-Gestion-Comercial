package com.gestion.sgc;

import com.gestion.sgc.domain.ports.inputs.CajaIn;
import com.gestion.sgc.infraestructure.controllers.CajaController;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CajaController.class)
@Import({SecurityConfig.class, JwtTokenValidatorFilter.class, CustomAuthenticationEntryPoint.class,
        CustomAccessDeniendHandler.class, InventoryExceptionHandler.class})
class CajaControllerTests {
    @Autowired MockMvc mvc;
    @MockitoBean CajaIn cajas;
    @MockitoBean UserDetailsService userDetails;
    @MockitoBean JwtService jwtService;
    @MockitoBean TokenBlacklistService blacklist;

    @Test void anonymousGets401() throws Exception {
        mvc.perform(get("/api/v1/cajas")).andExpect(status().isUnauthorized());
    }

    @Test void assistantCanReadButCannotOpen() throws Exception {
        mvc.perform(get("/api/v1/cajas").with(user("assistant").roles("ASISTENTE")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/cajas/1/sesion-actual").with(user("assistant").roles("ASISTENTE")))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/cajas/1/aperturas").with(user("assistant").roles("ASISTENTE"))
                        .contentType("application/json").content("{\"montoInicial\":0}"))
                .andExpect(status().isForbidden());
    }

    @Test void adminCanOpenAndUsernameComesFromAuthentication() throws Exception {
        mvc.perform(post("/api/v1/cajas/1/aperturas").with(user("admin-real").roles("ADMIN"))
                        .contentType("application/json").content("{\"montoInicial\":25.50}"))
                .andExpect(status().isCreated());
        verify(cajas).abrir(1L, new BigDecimal("25.50"), "admin-real");
    }

    @Test void negativeAmountReturns400WithoutUseCaseCall() throws Exception {
        mvc.perform(post("/api/v1/cajas/1/aperturas").with(user("admin").roles("ADMIN"))
                        .contentType("application/json").content("{\"montoInicial\":-1}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.montoInicial").exists());
        verifyNoInteractions(cajas);
    }
}
