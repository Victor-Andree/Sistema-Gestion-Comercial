package com.gestion.sgc.infraestructure.controllers;

import com.gestion.sgc.application.dto.request.MovimientoCajaRequest;
import com.gestion.sgc.application.dto.response.MovimientoCajaResponse;
import com.gestion.sgc.application.dto.response.ResumenCajaResponse;
import com.gestion.sgc.domain.ports.inputs.MovimientoCajaIn;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cajas/sesiones/{id}")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ASISTENTE')")
public class MovimientoCajaController {
    private final MovimientoCajaIn movimientoCajaIn;

    @PostMapping("/movimientos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovimientoCajaResponse> registrar(@PathVariable Long id,
                                                             @Valid @RequestBody MovimientoCajaRequest request,
                                                             Authentication authentication,
                                                             @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movimientoCajaIn.registrar(id, request, authentication.getName(), idempotencyKey));
    }

    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoCajaResponse>> listar(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoCajaIn.listar(id));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenCajaResponse> resumen(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoCajaIn.resumen(id));
    }
}
