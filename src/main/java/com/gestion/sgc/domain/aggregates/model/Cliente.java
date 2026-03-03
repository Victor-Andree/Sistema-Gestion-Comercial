package com.gestion.sgc.domain.aggregates.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    private Long clienteId;
    private Persona persona;
    private LocalDateTime fechaRegistro;

}
