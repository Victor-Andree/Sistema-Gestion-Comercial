package com.gestion.sgc.application.dto.request;


import com.gestion.sgc.domain.aggregates.constans.EstadoProducto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoRequest {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "El precio de venta es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero")
    private Double precioVenta;

    @NotNull(message = "El stock mínimo es obligatorio")
    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    @NotNull(message = "El tipo de producto es obligatorio")
    private Long tipoProductoId;

    @jakarta.validation.constraints.Pattern(regexp = "ACTIVO|INACTIVO", message = "El estado debe ser ACTIVO o INACTIVO")
    private String estado;

    @PositiveOrZero(message = "El stock inicial no puede ser negativo")
    private Integer cantidadInicial;

}
