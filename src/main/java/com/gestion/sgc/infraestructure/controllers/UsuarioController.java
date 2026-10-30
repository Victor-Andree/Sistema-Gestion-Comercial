package com.gestion.sgc.infraestructure.controllers;

import com.gestion.sgc.application.dto.request.ActualizarUsuarioRequest;
import com.gestion.sgc.application.dto.request.CambiarEstadoUsuarioRequest;
import com.gestion.sgc.application.dto.request.RegistrarUsuarioRequest;
import com.gestion.sgc.application.dto.response.UsuarioResponse;
import com.gestion.sgc.domain.ports.inputs.auth.AdministrarUsuarioIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {
    private final AdministrarUsuarioIn administrarUsuario;

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(administrarUsuario.listar());
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody RegistrarUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(administrarUsuario.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id,
                                                       @Valid @RequestBody ActualizarUsuarioRequest request,
                                                       Authentication authentication) {
        return ResponseEntity.ok(administrarUsuario.actualizar(id, request, authentication.getName()));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(@PathVariable Long id,
                                                          @Valid @RequestBody CambiarEstadoUsuarioRequest request,
                                                          Authentication authentication) {
        return ResponseEntity.ok(administrarUsuario.cambiarEstado(id, request, authentication.getName()));
    }
}
