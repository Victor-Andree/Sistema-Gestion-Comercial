package com.gestion.sgc.infraestructure.mapper;

import com.gestion.sgc.application.dto.request.DetalleVentaRequest;
import com.gestion.sgc.application.dto.request.VentaRequest;
import com.gestion.sgc.application.dto.response.ComprobanteResponse;
import com.gestion.sgc.application.dto.response.DetalleVentaResponse;
import com.gestion.sgc.application.dto.response.VentaResponse;
import com.gestion.sgc.domain.aggregates.model.Comprobante;
import com.gestion.sgc.domain.aggregates.model.DetalleVenta;
import com.gestion.sgc.domain.aggregates.model.Venta;
import com.gestion.sgc.infraestructure.entity.ComprobanteEntity;
import com.gestion.sgc.infraestructure.entity.DetalleVentaEntity;
import com.gestion.sgc.infraestructure.entity.VentaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProductoMapper.class,
        ClienteMapper.class, UsuarioMapper.class})
public interface VentaMapper {

    Venta toDomainFromEntity(VentaEntity entity);


    VentaEntity toEntity(Venta venta);


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


    @Mapping(target = "venta", ignore = true)
    DetalleVentaEntity detalleVentaToDetalleVentaEntity(DetalleVenta detalle);





}
