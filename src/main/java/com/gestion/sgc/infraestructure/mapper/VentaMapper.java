package com.gestion.sgc.infraestructure.mapper;

import com.gestion.sgc.application.dto.request.VentaRequest;
import com.gestion.sgc.application.dto.response.VentaResponse;
import com.gestion.sgc.domain.aggregates.model.Venta;
import com.gestion.sgc.infraestructure.entity.VentaEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
        ProductoMapper.class,
        ClienteMapper.class,
        UsuarioMapper.class,
        DetalleVentaMapper.class,
        ComprobanteMapper.class
})
public interface VentaMapper {

    Venta toDomainFromEntity(VentaEntity entity);

    Venta toDomain(VentaEntity entity);

    VentaEntity toEntity(Venta venta);
    @AfterMapping
    default void linkDetalles(@MappingTarget VentaEntity entity) {
        if (entity.getDetalles() != null) {
            entity.getDetalles().forEach(detalle -> detalle.setVenta(entity));
        }
    }


    @Mapping(target = "usuarioId", source = "usuario.usuarioId")
    @Mapping(target = "usuarioNombre", source = "usuario.persona.nombre")
    @Mapping(target = "clienteId", source = "cliente.clienteId")
    @Mapping(target = "clienteNombre", source = "cliente.persona.nombre")
    @Mapping(target = "estado", source = "estado")
    VentaResponse toResponse(Venta venta);

    List<VentaResponse> toResponseList(List<Venta> ventas);

    @Mapping(target = "ventaId", ignore = true)
    @Mapping(target = "fechaVenta", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    @Mapping(target = "comprobante", ignore = true)
    Venta toDomainFromRequest(VentaRequest request);






}
