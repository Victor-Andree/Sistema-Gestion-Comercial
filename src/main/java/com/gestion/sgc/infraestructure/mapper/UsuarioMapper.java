package com.gestion.sgc.infraestructure.mapper;

import com.gestion.sgc.application.dto.UsuarioDto;
import com.gestion.sgc.domain.aggregates.model.Usuario;
import com.gestion.sgc.infraestructure.entity.UsuarioEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mappings({

            @Mapping(source= "id", target = "usuarioId"),
            @Mapping(source="username" , target = "username"),
            @Mapping(source="password", target = "password"),
            @Mapping(source = "estado", target = "estado"),
            @Mapping(source = "fechaRegistro", target = "fechaRegistro"),
            @Mapping(source = "rol", target = "rol"),
    })
    Usuario toUsuario(UsuarioEntity usuarioEntity);

    @InheritInverseConfiguration
    UsuarioEntity toUsuarioEntity(Usuario usuario);

    @Mappings({

            @Mapping(source= "id", target = "usuarioId"),
            @Mapping(source="username" , target = "username"),
            @Mapping(source="password", target = "password"),
            @Mapping(source = "estado", target = "estado"),
            @Mapping(source = "fechaRegistro", target = "fechaRegistro"),
            @Mapping(source = "rol", target = "rol"),
    })
    UsuarioDto toUsuarioDto(Usuario usuario);

    @Mappings({

            @Mapping(source= "id", target = "usuarioId"),
            @Mapping(source="username" , target = "username"),
            @Mapping(source="password", target = "password"),
            @Mapping(source = "estado", target = "estado"),
            @Mapping(source = "fechaRegistro", target = "fechaRegistro"),
            @Mapping(source = "rol", target = "rol"),
    })
    Usuario toUsuarioFromtDto(UsuarioDto usuarioDto);

    @Mappings({

            @Mapping(source= "id", target = "usuarioId"),
            @Mapping(source="username" , target = "username"),
            @Mapping(source="password", target = "password"),
            @Mapping(source = "estado", target = "estado"),
            @Mapping(source = "fechaRegistro", target = "fechaRegistro"),
            @Mapping(source = "rol", target = "rol"),
    })
    List<UsuarioDto> toUsuarioDto(List<Usuario> usuarios);



}
