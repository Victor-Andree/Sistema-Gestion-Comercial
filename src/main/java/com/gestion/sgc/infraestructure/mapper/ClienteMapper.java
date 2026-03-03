package com.gestion.sgc.infraestructure.mapper;

import com.gestion.sgc.application.dto.response.ClienteResponse;
import com.gestion.sgc.domain.aggregates.model.Cliente;
import com.gestion.sgc.domain.aggregates.model.Persona;
import com.gestion.sgc.infraestructure.entity.ClienteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper (componentModel = "spring")
public interface ClienteMapper {

    // Entity to Domain
    @Mapping(source = "clienteId", target = "clienteId")
    @Mapping(source = "fechaRegistro", target = "fechaRegistro")
    @Mapping(source = "persona", target = "persona")
    Cliente toDomainFromEntity(ClienteEntity entity);

    // Domain to Entity
    @Mapping(source = "clienteId", target = "clienteId")
    @Mapping(source = "fechaRegistro", target = "fechaRegistro")
    @Mapping(source = "persona", target = "persona")
    ClienteEntity toEntity(Cliente cliente);

    // Domain to Response
    @Mapping(source = "cliente.clienteId", target = "clienteId")
    @Mapping(source = "persona.personaId", target = "personaId")
    @Mapping(source = "persona.nombre", target = "nombre")
    @Mapping(source = "persona.apellidos", target = "apellidos")
    @Mapping(source = "persona.dni", target = "dni")
    @Mapping(source = "persona.correo", target = "correo")
    @Mapping(source = "persona.telefono", target = "telefono")
    @Mapping(source = "cliente.fechaRegistro", target = "fechaRegistro")
    ClienteResponse toResponse(Cliente cliente, Persona persona);

}
