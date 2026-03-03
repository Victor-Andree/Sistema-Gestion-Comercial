package com.gestion.sgc.infraestructure.controllers;

import com.gestion.sgc.application.dto.request.VentaRequest;
import com.gestion.sgc.application.dto.response.VentaResponse;
import com.gestion.sgc.domain.ports.inputs.VentaIn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Endpoints para gestión de ventas")
@PreAuthorize("hasAnyAuthority('ADMIN', 'ASISTENTE')")
public class VentaController {

    private final VentaIn ventaIn;

    @Operation(summary = "Registrar una nueva venta")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ASISTENTE')")
    public ResponseEntity<VentaResponse> registrarVenta(@Valid @RequestBody VentaRequest request) {
        VentaResponse response = ventaIn.registrarVenta(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Registrar una venta como PENDIENTE (reserva)")
    @PostMapping("/pendiente")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ASISTENTE')")
    public ResponseEntity<VentaResponse> registrarVentaPendiente(
            @Valid @RequestBody VentaRequest request) {

        VentaResponse response = ventaIn.registrarVentaPendiente(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Confirmar una venta pendiente y convertirla en COMPLETADA")
    @PutMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ASISTENTE')")
    public ResponseEntity<VentaResponse> confirmarVenta(@PathVariable Long id) {

        VentaResponse response = ventaIn.confirmarVenta(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar ventas pendientes (reservas activas)")
    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ASISTENTE')")
    public ResponseEntity<List<VentaResponse>> listarVentasPendientes() {
        return ResponseEntity.ok(ventaIn.listarVentasPendientes());
    }


    @Operation(summary = "Buscar venta por ID")
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> buscarPorId(@PathVariable Long id) {
        return ventaIn.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar ventas por fecha")
    @GetMapping("/fecha")
    public ResponseEntity<List<VentaResponse>> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(ventaIn.listarPorFecha(fecha));
    }

    @Operation(summary = "Listar ventas por cliente")
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<VentaResponse>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(ventaIn.listarPorCliente(clienteId));
    }

    @Operation(summary = "Listar ventas por usuario (vendedor)")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<VentaResponse>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(ventaIn.listarPorUsuario(usuarioId));
    }

    @Operation(summary = "Listar todas las ventas")
    @GetMapping
    public ResponseEntity<List<VentaResponse>> listarTodas() {
        return ResponseEntity.ok(ventaIn.listarTodas());
    }

    @Operation(summary = "Anular una venta")
    @PutMapping("/{id}/anular")
    @PreAuthorize("hasAuthority('ADMIN')") // Solo ADMIN puede anular
    public ResponseEntity<VentaResponse> anularVenta(
            @PathVariable Long id,
            @RequestParam String motivo) {
        VentaResponse response = ventaIn.anularVenta(id, motivo);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener total de ventas del día")
    @GetMapping("/total-dia")
    public ResponseEntity<Double> obtenerTotalVentasDelDia() {
        return ResponseEntity.ok(ventaIn.obtenerTotalVentasDelDia());
    }

    @Operation(summary = "Listar ventas por rango de fechas")
    @GetMapping("/rango")
    public ResponseEntity<List<VentaResponse>> listarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(ventaIn.listarPorRangoFechas(desde, hasta));
    }



}
