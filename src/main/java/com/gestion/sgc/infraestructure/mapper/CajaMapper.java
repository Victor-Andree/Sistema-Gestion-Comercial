package com.gestion.sgc.infraestructure.mapper;

import com.gestion.sgc.domain.aggregates.model.Caja;
import com.gestion.sgc.infraestructure.entity.CajaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CajaMapper {
    Caja toDomain(CajaEntity entity);
    CajaEntity toEntity(Caja domain);
}
