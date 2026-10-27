package com.gestion.sgc.infraestructure.mapper;

import com.gestion.sgc.domain.aggregates.model.SesionCaja;
import com.gestion.sgc.infraestructure.entity.SesionCajaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CajaMapper.class, UsuarioMapper.class})
public interface SesionCajaMapper {
    SesionCaja toDomain(SesionCajaEntity entity);
    SesionCajaEntity toEntity(SesionCaja domain);
}
