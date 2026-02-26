package com.gestion.sgc.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrarUsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @Pattern(regexp = "^[0-9]{8}$", message = "DNI debe tener 8 dígitos")
    private String dni;

    @Email(message = "Correo inválido")
    private String correo;

    @Pattern(regexp = "^[0-9]{9}$", message = "Teléfono debe tener 9 dígitos")
    private String telefono;

    @NotBlank(message = "Username obligatorio")
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank(message = "Password obligatorio")
    @Size(min = 6)
    private String password;

    @NotBlank(message = "Rol obligatorio")
    private String rol;

}
