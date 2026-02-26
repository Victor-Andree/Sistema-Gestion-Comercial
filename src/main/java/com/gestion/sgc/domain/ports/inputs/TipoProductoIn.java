package com.gestion.sgc.domain.ports.inputs;

import com.gestion.sgc.application.dto.request.TipoProductoRequest;
import com.gestion.sgc.application.dto.response.TipoProductoResponse;

import java.util.List;
import java.util.Optional;

public interface TipoProductoIn {

    TipoProductoResponse createTipoProducto(TipoProductoRequest request);

    Optional<TipoProductoResponse> findById(Long id);

    List<TipoProductoResponse> getAllTipoProductos();

    Optional<TipoProductoResponse> findByNombre(String nombre);

    void deleteTipoProducto(Long id);


}
