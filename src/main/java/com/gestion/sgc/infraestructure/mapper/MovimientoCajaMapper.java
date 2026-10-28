package com.gestion.sgc.infraestructure.mapper;

import com.gestion.sgc.domain.aggregates.model.MovimientoCaja;
import com.gestion.sgc.infraestructure.entity.MovimientoCajaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {SesionCajaMapper.class, UsuarioMapper.class})
public interface MovimientoCajaMapper {
    MovimientoCaja toDomain(MovimientoCajaEntity entity);
    MovimientoCajaEntity toEntity(MovimientoCaja domain);
}
