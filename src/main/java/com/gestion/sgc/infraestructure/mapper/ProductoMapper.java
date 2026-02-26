package com.gestion.sgc.infraestructure.mapper;


import com.gestion.sgc.application.dto.request.ProductoRequest;
import com.gestion.sgc.application.dto.response.ProductoResponse;
import com.gestion.sgc.domain.aggregates.model.Producto;
import com.gestion.sgc.infraestructure.entity.ProductoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    // Entity to Domain
    @Mapping(source = "estadoProducto", target = "estadoProducto")
    @Mapping(source = "tipoProducto", target = "tipoProducto")
    Producto toDomainFromEntity(ProductoEntity entity);

    @Mapping(source = "estadoProducto", target = "estadoProducto")
    @Mapping(source = "tipoProducto", target = "tipoProducto")
    ProductoEntity toEntity(Producto producto);

    // Request to Domain
    @Mapping(target = "productoId", ignore = true)
    @Mapping(target = "tipoProducto", ignore = true)
    @Mapping(target = "estadoProducto", ignore = true)
    @Mapping(source = "stockMinimo", target = "stockMinimo")
    Producto toDomainFromRequest(ProductoRequest request);

    // Domain to Response
    @Mapping(source = "tipoProducto.tipoProductoId", target = "tipoProductoId")
    @Mapping(source = "tipoProducto.nombre", target = "tipoProductoNombre")
    @Mapping(source = "estadoProducto", target = "estadoProducto")
    ProductoResponse toResponse(Producto producto);

    // Listas
    List<ProductoResponse> toResponseList(List<Producto> productos);


}
