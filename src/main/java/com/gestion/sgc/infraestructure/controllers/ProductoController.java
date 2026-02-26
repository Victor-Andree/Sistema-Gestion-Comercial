package com.gestion.sgc.infraestructure.controllers;


import com.gestion.sgc.application.dto.request.ProductoRequest;
import com.gestion.sgc.application.dto.response.ProductoResponse;
import com.gestion.sgc.domain.ports.inputs.ProductosIn;
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
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Endpoint para gestionar productos")
@PreAuthorize("hasAnyAuthority('ADMIN', 'ASISTENTE')")
public class ProductoController {

    private final ProductosIn productoIn;

    @Operation(summary = "Crear nuevo producto (pez, filtro, alimento, etc.)")
    @PostMapping
    public ResponseEntity<ProductoResponse> crearProducto(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse response = productoIn.createProducto(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar producto existente")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {
        ProductoResponse response = productoIn.UpdateProducto(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar producto por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> buscarPorId(@PathVariable Long id) {
        return productoIn.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar todos los productos")
    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listarTodos() {
        List<ProductoResponse> productos = productoIn.getAllProductos();
        return ResponseEntity.ok(productos);
    }

    @Operation(summary = "Listar productos por tipo")
    @GetMapping("/tipo/{tipoProductoId}")
    public ResponseEntity<List<ProductoResponse>> listarPorTipo(@PathVariable Long tipoProductoId) {
        List<ProductoResponse> productos = productoIn.getAllTipos(tipoProductoId);
        return ResponseEntity.ok(productos);
    }

    @Operation(summary = "Listar productos con stock bajo (para reabastecer)")
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<ProductoResponse>> listarStockBajo() {
        List<ProductoResponse> productos = productoIn.getAllStockMinimo();
        return ResponseEntity.ok(productos);
    }

    @Operation(summary = "Cambiar estado del producto (ACTIVO/INACTIVO)")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {
        productoIn.cambiarEstado(id, estado);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Eliminar producto")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoIn.deleteProducto(id);
        return ResponseEntity.noContent().build();
    }




}
