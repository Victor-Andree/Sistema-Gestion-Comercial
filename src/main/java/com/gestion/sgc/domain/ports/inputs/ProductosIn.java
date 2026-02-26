package com.gestion.sgc.domain.ports.inputs;

import com.gestion.sgc.application.dto.request.ProductoRequest;
import com.gestion.sgc.application.dto.response.ProductoResponse;

import java.util.List;
import java.util.Optional;

public interface ProductosIn {

    ProductoResponse createProducto(ProductoRequest request);

    ProductoResponse UpdateProducto(Long id, ProductoRequest request);

    Optional<ProductoResponse> findById(Long id);

    List<ProductoResponse> getAllProductos();

    List<ProductoResponse> getAllTipos(Long tipoProductoId);

    List<ProductoResponse> getAllStockMinimo();

    void deleteProducto(Long id);

    void cambiarEstado(Long id, String estado);



}
