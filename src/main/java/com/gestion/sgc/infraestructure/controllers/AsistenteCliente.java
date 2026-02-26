package com.gestion.sgc.infraestructure.controllers;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/asistente/clientes")
@RequiredArgsConstructor
@Tag(name = "Asistente - Clientes", description = "Endpoint para que ASISTENTE cree clientes")
@SecurityRequirement(name = "bearerAuth")
public class AsistenteCliente {


}
