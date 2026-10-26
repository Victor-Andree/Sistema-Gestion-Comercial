package com.gestion.sgc;

import com.gestion.sgc.domain.ports.inputs.*;
import com.gestion.sgc.infraestructure.controllers.*;
import com.gestion.sgc.infraestructure.exception.InventoryExceptionHandler;
import com.gestion.sgc.infraestructure.security.config.SecurityConfig;
import com.gestion.sgc.infraestructure.security.filters.JwtTokenValidatorFilter;
import com.gestion.sgc.infraestructure.security.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ProductoController.class, StockController.class, TipoProductoController.class})
@Import({SecurityConfig.class, JwtTokenValidatorFilter.class, CustomAuthenticationEntryPoint.class,
        CustomAccessDeniendHandler.class, InventoryExceptionHandler.class})
class InventoryControllerTests {
    @Autowired MockMvc mvc;
    @MockitoBean ProductosIn products;
    @MockitoBean StockIn stocks;
    @MockitoBean TipoProductoIn categories;
    @MockitoBean UserDetailsService userDetails;
    @MockitoBean JwtService jwtService;
    @MockitoBean TokenBlacklistService blacklist;

    @Test void requiresAuthentication() throws Exception {
        mvc.perform(get("/api/v1/stocks")).andExpect(status().isUnauthorized());
    }

    @Test void rejectsOtherRolesForInventory() throws Exception {
        for (String path : new String[]{"/api/v1/stocks", "/api/v1/tiposproducto", "/api/v1/productos"}) {
            mvc.perform(get(path).with(user("other").roles("OTHER"))).andExpect(status().isForbidden());
        }
    }

    @Test void assistantCanReadAllInventoryResources() throws Exception {
        for (String path : new String[]{"/api/v1/stocks", "/api/v1/tiposproducto", "/api/v1/productos"}) {
            mvc.perform(get(path).with(user("assistant").roles("ASISTENTE"))).andExpect(status().isOk());
        }
    }

    @Test void assistantCannotMutateInventory() throws Exception {
        String validProduct = "{\"nombre\":\"Filtro\",\"descripcion\":\"Filtro externo\","
                + "\"precioVenta\":10,\"stockMinimo\":2,\"cantidadInicial\":5,"
                + "\"tipoProductoId\":1,\"estado\":\"ACTIVO\"}";
        mvc.perform(patch("/api/v1/stocks/adjust/1").param("realQuantity", "0")
                .with(user("assistant").roles("ASISTENTE"))).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/stocks/1").param("quantity", "1").param("type", "INCOME")
                .with(user("assistant").roles("ASISTENTE"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/productos").contentType("application/json").content(validProduct)
                .with(user("assistant").roles("ASISTENTE"))).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/productos/1").contentType("application/json").content(validProduct)
                .with(user("assistant").roles("ASISTENTE"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/tiposproducto").contentType("application/json").content("{\"nombre\":\"Filtros\"}")
                .with(user("assistant").roles("ASISTENTE"))).andExpect(status().isForbidden());
        verifyNoInteractions(products, stocks, categories);
    }

    @Test void adminCanAdjust() throws Exception {
        mvc.perform(patch("/api/v1/stocks/adjust/1").param("realQuantity", "0")
                .with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        verify(stocks).adjustStock(1L, 0, null);
    }

    @Test void allowsPatchPreflightFromFrontend() throws Exception {
        mvc.perform(options("/api/v1/productos/1/estado").header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "PATCH")
                .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test void invalidProductReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/v1/productos").with(user("admin").roles("ADMIN"))
                .contentType("application/json").content("{\"nombre\":\" \",\"precioVenta\":-1,\"cantidadInicial\":-1,\"estado\":\"OTRO\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.nombre").exists())
                .andExpect(jsonPath("$.errors.cantidadInicial").exists()).andExpect(jsonPath("$.errors.estado").exists());
        verifyNoInteractions(products);
    }

    @Test void businessErrorsExposeSafeMessage() throws Exception {
        doThrow(new ResponseStatusException(CONFLICT, "Stock insuficiente"))
                .when(stocks).updateStock(1L, 100, "OUTCOME");
        mvc.perform(put("/api/v1/stocks/1").param("quantity", "100").param("type", "OUTCOME")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("Stock insuficiente"));
    }
}
