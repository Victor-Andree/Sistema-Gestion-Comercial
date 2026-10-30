package com.gestion.sgc.infraestructure.controllers;

import com.gestion.sgc.application.dto.request.AperturaCajaRequest;
import com.gestion.sgc.application.dto.request.CierreCajaRequest;
import com.gestion.sgc.application.dto.response.CajaResponse;
import com.gestion.sgc.application.dto.response.SesionCajaResponse;
import com.gestion.sgc.domain.ports.inputs.CajaIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cajas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ASISTENTE')")
public class CajaController {
    private final CajaIn cajaIn;

    @GetMapping
    public ResponseEntity<List<CajaResponse>> listar() {
        return ResponseEntity.ok(cajaIn.listar());
    }

    @PostMapping("/{id}/aperturas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SesionCajaResponse> abrir(@PathVariable Long id,
                                                     @Valid @RequestBody AperturaCajaRequest request,
                                                     Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cajaIn.abrir(id, request.montoInicial(), authentication.getName()));
    }

    @GetMapping("/{id}/sesion-actual")
    public ResponseEntity<SesionCajaResponse> sesionActual(@PathVariable Long id) {
        return ResponseEntity.ok(cajaIn.obtenerSesionActual(id));
    }

    @PostMapping("/sesiones/{sesionId}/cierre")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SesionCajaResponse> cerrar(@PathVariable Long sesionId,
                                                      @Valid @RequestBody CierreCajaRequest request,
                                                      Authentication authentication) {
        return ResponseEntity.ok(cajaIn.cerrar(sesionId, request, authentication.getName()));
    }
}
