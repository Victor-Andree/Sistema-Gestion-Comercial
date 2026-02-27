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

    VentaMapper INSTANCE = Mappers.getMapper(VentaMapper.class);

    //  ENTITY to DOMAIN
    @Mapping(source = "usuario", target = "usuario")
    @Mapping(source = "cliente", target = "cliente")
    Venta toDomainFromEntity(VentaEntity entity);

    @Mapping(source = "usuario", target = "usuario")
    @Mapping(source = "cliente", target = "cliente")
    VentaEntity toEntity(Venta venta);

    //  DETALLE ENTITY to DOMAIN
    @Mapping(source = "producto", target = "producto")
    @Mapping(source = "venta", target = "venta")
    DetalleVenta toDomainFromEntity(DetalleVentaEntity entity);

    //  DETALLE DOMAIN TO ENTITY
    @Mapping(source = "producto", target = "producto")
    @Mapping(source = "venta", target = "venta")
    @Mapping(source = "cantidad", target = "cantidad")
    @Mapping(source = "precioUnitario", target = "precioUnitario")
    @Mapping(source = "subtotal", target = "subtotal")
    DetalleVentaEntity toEntity(DetalleVenta detalle);

    // COMPROBANTE ENTITY to DOMAIN
    @Mapping(source = "venta", target = "venta")
    Comprobante toDomainFromEntity(ComprobanteEntity entity);

    @Mapping(source = "venta", target = "venta")
    ComprobanteEntity toEntity(Comprobante comprobante);

    // REQUEST to DOMAIN
    @Mapping(target = "ventaId", ignore = true)
    @Mapping(target = "fechaVenta", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    Venta toDomainFromRequest(VentaRequest request);

    // DETALLE REQUEST to DOMAIN
    @Mapping(target = "detalleVentaId", ignore = true)
    @Mapping(target = "precioUnitario", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "venta", ignore = true)
    @Mapping(target = "producto", ignore = true)
    DetalleVenta toDomainFromRequest(DetalleVentaRequest request);

    // DOMAIN to RESPONSE
    @Mapping(source = "usuario.usuarioId", target = "usuarioId")
    @Mapping(source = "usuario.username", target = "usuarioNombre")
    @Mapping(source = "cliente.clienteId", target = "clienteId")
    @Mapping(source = "cliente.persona.nombre", target = "clienteNombre")
    @Mapping(source = "fechaVenta", target = "fechaVenta")
    VentaResponse toResponse(Venta venta);

    // DETALLE DOMAIN to RESPONSE
    @Mapping(source = "producto.productoId", target = "productoId")
    @Mapping(source = "producto.nombre", target = "productoNombre")
    DetalleVentaResponse toResponse(DetalleVenta detalle);

    //  COMPROBANTE DOMAIN to RESPONSE
    ComprobanteResponse toResponse(Comprobante comprobante);

    // LISTAS
    List<VentaResponse> toResponseList(List<Venta> ventas);
    List<DetalleVentaResponse> toDetalleResponseList(List<DetalleVenta> detalles);


}
