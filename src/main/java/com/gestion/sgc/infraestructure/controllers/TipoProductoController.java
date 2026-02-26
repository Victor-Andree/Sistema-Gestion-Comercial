package com.gestion.sgc.infraestructure.controllers;


import com.gestion.sgc.application.dto.request.TipoProductoRequest;
import com.gestion.sgc.application.dto.response.TipoProductoResponse;
import com.gestion.sgc.domain.ports.inputs.TipoProductoIn;
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
@RequestMapping("/api/v1/tiposproducto")
@RequiredArgsConstructor
@Tag(name = "Tipos de Producto", description = "Gestión de categorías de productos")
@PreAuthorize("hasAnyAuthority('ADMIN', 'ASISTENTE')")
public class TipoProductoController {

    private final TipoProductoIn tipoProductoIn;

    @Operation(summary = "Crear nuevo tipo de producto")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')") // Solo ADMIN puede crear
    public ResponseEntity<TipoProductoResponse> crearTipoProducto(@Valid @RequestBody TipoProductoRequest request) {
        TipoProductoResponse response = tipoProductoIn.createTipoProducto(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Listar todos los tipos")
    @GetMapping
    public ResponseEntity<List<TipoProductoResponse>> listarTodos() {
        List<TipoProductoResponse> tipos = tipoProductoIn.getAllTipoProductos();
        return ResponseEntity.ok(tipos);
    }

    @Operation(summary = "Buscar tipo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<TipoProductoResponse> buscarPorId(@PathVariable Long id) {
        return tipoProductoIn.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar tipo por nombre")
    @GetMapping("/buscar")
    public ResponseEntity<TipoProductoResponse> buscarPorNombre(@RequestParam String nombre) {
        return tipoProductoIn.findByNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar tipo de producto")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> eliminarTipoProducto(@PathVariable Long id) {
        tipoProductoIn.deleteTipoProducto(id);
        return ResponseEntity.noContent().build();
    }





}
