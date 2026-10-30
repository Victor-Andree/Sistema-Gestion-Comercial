package com.gestion.sgc;

import com.gestion.sgc.domain.ports.inputs.auth.LoginUsuarioIn;
import com.gestion.sgc.domain.ports.inputs.auth.RegistrarUsuarioIn;
import com.gestion.sgc.infraestructure.controllers.AuthController;
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

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtTokenValidatorFilter.class, CustomAuthenticationEntryPoint.class,
        CustomAccessDeniendHandler.class, InventoryExceptionHandler.class})
class AuthRegistrationSecurityTests {
    @Autowired MockMvc mvc;
    @MockitoBean LoginUsuarioIn login;
    @MockitoBean RegistrarUsuarioIn registration;
    @MockitoBean UserDetailsService userDetails;
    @MockitoBean JwtService jwtService;
    @MockitoBean TokenBlacklistService blacklist;

    @Test
    void publicAndAssistantCannotRegisterUsersButAdminCan() throws Exception {
        mvc.perform(post("/api/v1/authentication/registro")
                        .contentType("application/json").content(validBody()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/authentication/registro")
                        .with(user("assistant").roles("ASISTENTE"))
                        .contentType("application/json").content(validBody()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(registration);

        mvc.perform(post("/api/v1/authentication/registro")
                        .with(user("admin").roles("ADMIN"))
                        .contentType("application/json").content(validBody()))
                .andExpect(status().isCreated());
    }

    private String validBody() {
        return """
                {"nombre":"Ana","apellidos":"Torres","dni":"12345678","correo":"ana@example.com",
                "telefono":"987654321","username":"atorres","password":"secreto1","rol":"ASISTENTE"}
                """;
    }
}
