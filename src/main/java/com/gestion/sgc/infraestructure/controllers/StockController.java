package com.gestion.sgc.infraestructure.controllers;


import com.gestion.sgc.application.dto.request.StockRequest;
import com.gestion.sgc.application.dto.response.StockResponse;
import com.gestion.sgc.domain.ports.inputs.StockIn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "Endpoints para gestión de inventario")
@PreAuthorize("hasAnyAuthority('ADMIN', 'ASISTENTE')")
public class StockController {

    private final StockIn stockIn;

    @Operation(summary = "Crear stock inicial para un producto nuevo")
    @PostMapping("/initial")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ASISTENTE')")
    public ResponseEntity<StockResponse> createInitialStock(@Valid @RequestBody StockRequest request) {
        StockResponse response = stockIn.createInitialStock(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener stock por ID de producto")
    @GetMapping("/product/{productId}")
    public ResponseEntity<StockResponse> getStockByProductId(@PathVariable Long productId) {
        return stockIn.getStockByProductId(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar todos los stocks")
    @GetMapping
    public ResponseEntity<List<StockResponse>> listAllStocks() {
        return ResponseEntity.ok(stockIn.listAllStocks());
    }

    @Operation(summary = "Listar productos con stock bajo")
    @GetMapping("/low-stock")
    public ResponseEntity<List<StockResponse>> listLowStock() {
        return ResponseEntity.ok(stockIn.listLowStock());
    }

    @Operation(summary = "Actualizar stock (ingreso o salida)")
    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ASISTENTE')")
    public ResponseEntity<StockResponse> updateStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            @RequestParam String type) { // "INCOME" o "OUTCOME"
        StockResponse response = stockIn.updateStock(productId, quantity, type);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Ajustar stock por inventario físico")
    @PatchMapping("/adjust/{productId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StockResponse> adjustStock(
            @PathVariable Long productId,
            @RequestParam Integer realQuantity,
            @RequestParam(required = false) String observation) {
        StockResponse response = stockIn.adjustStock(productId, realQuantity, observation);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verificar disponibilidad de stock")
    @GetMapping("/check/{productId}")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(stockIn.checkAvailability(productId, quantity));
    }


}
