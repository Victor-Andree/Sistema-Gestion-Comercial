package com.gestion.sgc.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CambiarEstadoUsuarioRequest {
    @NotBlank(message = "Estado obligatorio")
    private String estado;
}
