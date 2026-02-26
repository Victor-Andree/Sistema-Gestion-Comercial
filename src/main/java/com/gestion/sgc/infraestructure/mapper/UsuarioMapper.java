package com.gestion.sgc.infraestructure.mapper;

import com.gestion.sgc.application.dto.request.RegistrarUsuarioRequest;
import com.gestion.sgc.application.dto.response.UsuarioResponse;
import com.gestion.sgc.domain.aggregates.model.Persona;
import com.gestion.sgc.domain.aggregates.model.Usuario;
import com.gestion.sgc.infraestructure.entity.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {


    UsuarioMapper INSTANCE = Mappers.getMapper(UsuarioMapper.class);


    // DOMAIN TO ENTITY
    Usuario toDomainFromEntity(UsuarioEntity entity);

    // ENTITY TO DOMAIN
    UsuarioEntity toEntity(Usuario usuario);


    // Response combinado (Usuario + Persona)
    @Mapping(source = "usuario.usuarioId", target = "usuarioId")
    @Mapping(source = "persona.personaId", target = "personaId")
    @Mapping(source = "persona.nombre", target = "nombre")
    @Mapping(source = "persona.apellidos", target = "apellidos")
    @Mapping(source = "usuario.username", target = "username")
    @Mapping(source = "usuario.rol", target = "rol")
    @Mapping(source = "usuario.estadoUsuario", target = "estado")
    UsuarioResponse toResponse(Usuario usuario, Persona persona);



}
