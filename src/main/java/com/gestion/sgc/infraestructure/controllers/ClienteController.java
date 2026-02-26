package com.gestion.sgc.infraestructure.controllers;


import com.gestion.sgc.application.dto.request.ClienteRequest;
import com.gestion.sgc.application.dto.response.ClienteResponse;
import com.gestion.sgc.domain.ports.inputs.ClienteIn;
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
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Endpoint para gestionar clientes")
@PreAuthorize("hasAnyAuthority('ADMIN', 'ASISTENTE')")
public class ClienteController {


    private final ClienteIn clienteIn;

    @Operation(summary = "Crear nuevo cliente")
    @PostMapping
    public ResponseEntity<ClienteResponse> crearCliente(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteIn.createCliente(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Buscar cliente por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
        return clienteIn.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar todos los clientes")
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarTodos() {
        List<ClienteResponse> clientes = clienteIn.getAllClientes();
        return ResponseEntity.ok(clientes);
    }

    @Operation(summary = "Buscar cliente por DNI")
    @GetMapping("/dni/{dni}")
    public ResponseEntity<ClienteResponse> buscarPorDni(@PathVariable String dni) {
        return clienteIn.findByDni(dni)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar cliente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteIn.deleteCliente(id);
        return ResponseEntity.noContent().build();
    }


}
