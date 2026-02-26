package com.gestion.sgc.infraestructure.controllers;


import com.gestion.sgc.application.dto.request.LoginRequest;
import com.gestion.sgc.application.dto.request.RegistrarUsuarioRequest;
import com.gestion.sgc.application.dto.response.AuthenticactionResponse;
import com.gestion.sgc.application.dto.response.UsuarioResponse;
import com.gestion.sgc.domain.ports.inputs.auth.LoginUsuarioIn;
import com.gestion.sgc.domain.ports.inputs.auth.RegistrarUsuarioIn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/authentication")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "endpoint para gestionar la autenticacion de usuarios")
public class AuthController {

    private final LoginUsuarioIn loginUsuarioIn;
    private final RegistrarUsuarioIn registrarUsuarioIn;

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario y devuelve tokens JWT para acceso al sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación exitosa",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticactionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud incorrecta - validación fallida",
                    content = @Content
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthenticactionResponse> login(
            @Parameter(description = "Credenciales de acceso", required = true)
            @Valid @RequestBody LoginRequest request) {

        AuthenticactionResponse response = loginUsuarioIn.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrarUsuario(
            @Parameter(description = "Datos del nuevo usuario", required = true)
            @Valid @RequestBody RegistrarUsuarioRequest request) {

        UsuarioResponse response = registrarUsuarioIn.registrar(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }





}
