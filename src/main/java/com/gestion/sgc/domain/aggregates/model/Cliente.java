package com.gestion.sgc.domain.aggregates.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cliente {

    private Long clienteId;
    private Persona persona;
    private LocalDateTime fechaRegistro;

}
