package com.gestion.sgc.infraestructure.mapper;

import com.gestion.sgc.domain.aggregates.model.Usuario;
import com.gestion.sgc.infraestructure.entity.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {


    UsuarioMapper INSTANCE = Mappers.getMapper(UsuarioMapper.class);

    // ENTITY TO DOMAIN
    Usuario toDomainFromEntity(UsuarioEntity entity);

    // DOMAIN TO ENTITY
    UsuarioEntity toEntity(Usuario usuario);

}
