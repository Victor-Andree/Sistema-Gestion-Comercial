package com.gestion.sgc.infraestructure.mapper;


import com.gestion.sgc.domain.aggregates.model.TipoProducto;
import com.gestion.sgc.infraestructure.entity.TipoProductoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TipoProductoMapper {

    TipoProductoMapper INSTANCE = Mappers.getMapper(TipoProductoMapper.class);

    // Entity to Domain
    @Mapping(source = "tipoProductoId", target = "tipoProductoId")
    @Mapping(source = "nombre", target = "nombre")
    TipoProducto toDomainFromEntity(TipoProductoEntity entity);

    // Domain to  Entity
    @Mapping(source = "tipoProductoId", target = "tipoProductoId")
    @Mapping(source = "nombre", target = "nombre")
    TipoProductoEntity toEntity(TipoProducto tipoProducto);

    // Listas
    List<TipoProducto> toDomainList(List<TipoProductoEntity> entities);
    List<TipoProductoEntity> toEntityList(List<TipoProducto> domains);


}
