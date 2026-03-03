package com.gestion.sgc.infraestructure.mapper;


import com.gestion.sgc.application.dto.request.TipoProductoRequest;
import com.gestion.sgc.application.dto.response.TipoProductoResponse;
import com.gestion.sgc.domain.aggregates.model.TipoProducto;
import com.gestion.sgc.infraestructure.entity.TipoProductoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TipoProductoMapper {

    // ENTITY to DOMAIN
    TipoProducto toDomainFromEntity(TipoProductoEntity entity);
    TipoProductoEntity toEntity(TipoProducto tipoProducto);

    // REQUEST to DOMAIN
    @Mapping(target = "tipoProductoId", ignore = true)
    TipoProducto toDomainFromRequest(TipoProductoRequest request);

    //  DOMAIN to RESPONSE
    TipoProductoResponse toResponse(TipoProducto tipoProducto);

    //  LISTAS
    List<TipoProductoResponse> toResponseList(List<TipoProducto> tipos);

}
