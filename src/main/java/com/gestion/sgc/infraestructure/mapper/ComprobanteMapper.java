package com.gestion.sgc.infraestructure.mapper;

import com.gestion.sgc.application.dto.response.ComprobanteResponse;
import com.gestion.sgc.domain.aggregates.model.Comprobante;
import com.gestion.sgc.infraestructure.entity.ComprobanteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ComprobanteMapper {

    @Mapping(target = "venta", ignore = true)
    Comprobante toDomainFromEntity(ComprobanteEntity entity);

    @Mapping(target = "venta", ignore = true)
    ComprobanteEntity toEntity(Comprobante comprobante);

    ComprobanteResponse toResponse(Comprobante comprobante);



}
