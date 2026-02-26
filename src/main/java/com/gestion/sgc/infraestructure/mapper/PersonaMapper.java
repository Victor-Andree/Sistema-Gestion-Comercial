package com.gestion.sgc.infraestructure.mapper;

import com.gestion.sgc.application.dto.request.ClienteRequest;
import com.gestion.sgc.application.dto.request.PersonaRequest;
import com.gestion.sgc.application.dto.request.RegistrarUsuarioRequest;
import com.gestion.sgc.application.dto.response.PersonaResponse;
import com.gestion.sgc.domain.aggregates.model.Persona;
import com.gestion.sgc.infraestructure.entity.PersonaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PersonaMapper {

    PersonaMapper INSTANCE = Mappers.getMapper(PersonaMapper.class);

    //  DOMAIN TO ENTITY
    Persona toDomainFromEntity(PersonaEntity entity);

    // ENTITY TO DOMAIN
    PersonaEntity toEntity(Persona persona);

    // Domain to RequestDto
    Persona toDomainFromRequest(PersonaRequest request);

    //ResponseDto to Domain
    PersonaResponse toResponse(Persona persona);

    // List
    List<PersonaResponse> toResponseList(List<Persona> personas);

    @Mapping(target = "personaId", ignore = true)
    Persona toDomainFromRegistroRequest(RegistrarUsuarioRequest request);

    @Mapping(target = "personaId", ignore = true)
    Persona toDomainFromClienteRequest(ClienteRequest request);



}
