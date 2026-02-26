package com.gestion.sgc.infraestructure.controllers;


import com.gestion.sgc.application.dto.request.PersonaRequest;
import com.gestion.sgc.application.dto.response.PersonaResponse;
import com.gestion.sgc.domain.ports.inputs.PersonaIn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/personas")
@RequiredArgsConstructor
@Tag(name = "Personas", description = "Endpoint para gestionar personas")
public class PersonaController {

    private final PersonaIn personaIn;

    @Operation(summary = "Crear una nueva persona")
    @PostMapping
    public ResponseEntity<PersonaResponse> crearPersona(@Valid @RequestBody PersonaRequest request) {
        PersonaResponse response = personaIn.crearPersona(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Buscar persona por ID")
    @GetMapping("/{id}")
    public ResponseEntity<PersonaResponse> buscarPorId(@PathVariable Long id) {
        return personaIn.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar todas las personas")
    @GetMapping
    public ResponseEntity<List<PersonaResponse>> listarTodas() {
        List<PersonaResponse> personas = personaIn.listarTodas();
        return ResponseEntity.ok(personas);
    }



}
