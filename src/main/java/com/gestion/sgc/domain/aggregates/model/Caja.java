package com.gestion.sgc.domain.aggregates.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Caja {
    private Long cajaId;
    private String nombre;
    private boolean activa;
}
