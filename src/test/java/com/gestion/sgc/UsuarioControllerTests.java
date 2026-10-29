package com.gestion.sgc;

import com.gestion.sgc.application.dto.request.CambiarEstadoUsuarioRequest;
import com.gestion.sgc.domain.ports.inputs.auth.AdministrarUsuarioIn;
import com.gestion.sgc.infraestructure.controllers.UsuarioController;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import({SecurityConfig.class, JwtTokenValidatorFilter.class, CustomAuthenticationEntryPoint.class,
        CustomAccessDeniendHandler.class, InventoryExceptionHandler.class})
class UsuarioControllerTests {
    @Autowired MockMvc mvc;
    @MockitoBean AdministrarUsuarioIn usuarios;
    @MockitoBean UserDetailsService userDetails;
    @MockitoBean JwtService jwtService;
    @MockitoBean TokenBlacklistService blacklist;

    @Test void anonymousGets401AndAssistantGets403() throws Exception {
        mvc.perform(get("/api/v1/usuarios")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/usuarios").with(user("assistant").roles("ASISTENTE")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(usuarios);
    }

    @Test void adminCanListAndCreate() throws Exception {
        mvc.perform(get("/api/v1/usuarios").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/usuarios").with(user("admin").roles("ADMIN"))
                        .contentType("application/json").content(validCreateBody()))
                .andExpect(status().isCreated());
        verify(usuarios).listar();
    }

    @Test void invalidCreateReturns400WithoutCallingUseCase() throws Exception {
        mvc.perform(post("/api/v1/usuarios").with(user("admin").roles("ADMIN"))
                        .contentType("application/json").content(validCreateBody().replace("secreto1", "123")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(usuarios);
    }

    @Test void authenticatedUsernameIsUsedWhenChangingStatus() throws Exception {
        mvc.perform(patch("/api/v1/usuarios/7/estado").with(user("admin-real").roles("ADMIN"))
                        .contentType("application/json").content("{\"estado\":\"INACTIVO\"}"))
                .andExpect(status().isOk());
        verify(usuarios).cambiarEstado(7L, new CambiarEstadoUsuarioRequest("INACTIVO"), "admin-real");
    }

    private String validCreateBody() {
        return """
                {"nombre":"Ana","apellidos":"Torres","dni":"12345678","correo":"ana@example.com",
                "telefono":"987654321","username":"atorres","password":"secreto1","rol":"ASISTENTE"}
                """;
    }
}
