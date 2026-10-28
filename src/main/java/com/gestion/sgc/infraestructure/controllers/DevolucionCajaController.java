package com.gestion.sgc.infraestructure.controllers;

import com.gestion.sgc.application.dto.response.DevolucionCajaPendienteResponse;
import com.gestion.sgc.domain.ports.inputs.DevolucionCajaIn;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cajas/devoluciones")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ASISTENTE')")
public class DevolucionCajaController {
    private final DevolucionCajaIn devolucionCajaIn;

    @GetMapping("/pendientes")
    public ResponseEntity<List<DevolucionCajaPendienteResponse>> listarPendientes() {
        return ResponseEntity.ok(devolucionCajaIn.listarPendientes());
    }
}
