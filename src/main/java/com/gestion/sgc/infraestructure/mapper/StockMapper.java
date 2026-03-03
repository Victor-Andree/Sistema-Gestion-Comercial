package com.gestion.sgc.infraestructure.mapper;

import com.gestion.sgc.application.dto.request.StockRequest;
import com.gestion.sgc.application.dto.response.StockResponse;
import com.gestion.sgc.domain.aggregates.model.Stock;
import com.gestion.sgc.infraestructure.entity.StockEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMapper {

    // Entity to Domain
    @Mapping(source = "producto", target = "producto")
    Stock toDomainFromEntity(StockEntity entity);

    @Mapping(source = "producto", target = "producto")
    StockEntity toEntity(Stock stock);

    // Request to  Domain
    @Mapping(target = "stockId", ignore = true)
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    Stock toDomainFromRequest(StockRequest request);

    // Domain to  Response
    @Mapping(source = "producto.productoId", target = "productoId")
    @Mapping(source = "producto.nombre", target = "productoNombre")
    @Mapping(source = "producto.stockMinimo", target = "stockMinimo")
    StockResponse toResponse(Stock stock);

    // Listas
    List<StockResponse> toResponseList(List<Stock> stocks);



}
