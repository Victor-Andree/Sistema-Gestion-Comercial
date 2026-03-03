package com.gestion.sgc.infraestructure.mapper;

import com.gestion.sgc.application.dto.response.DetalleVentaResponse;
import com.gestion.sgc.domain.aggregates.model.DetalleVenta;
import com.gestion.sgc.infraestructure.entity.DetalleVentaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring",  uses = {ProductoMapper.class} )
public interface DetalleVentaMapper {

    // ENTITY → DOMAIN
    @Mapping(target = "venta", ignore = true)
    DetalleVenta toDomainFromEntity(DetalleVentaEntity entity);

    // DOMAIN → ENTITY
    @Mapping(target = "venta", ignore = true)
    DetalleVentaEntity toEntity(DetalleVenta detalle);

    // DOMAIN → RESPONSE
    @Mapping(source = "producto.productoId", target = "productoId")
    @Mapping(source = "producto.nombre", target = "productoNombre")
    DetalleVentaResponse toResponse(DetalleVenta detalle);

    List<DetalleVentaResponse> toResponseList(List<DetalleVenta> detalles);


}
