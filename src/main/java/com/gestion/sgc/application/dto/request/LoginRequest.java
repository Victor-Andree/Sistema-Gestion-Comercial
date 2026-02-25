package com.gestion.sgc.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "el username no puede estar vacio")
    private String username;

    @NotBlank(message ="ingrasa una contraseña valida")
    private String password;


}
