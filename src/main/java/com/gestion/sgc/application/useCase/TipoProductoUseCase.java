package com.gestion.sgc.application.useCase;


import com.gestion.sgc.application.dto.request.TipoProductoRequest;
import com.gestion.sgc.application.dto.response.TipoProductoResponse;
import com.gestion.sgc.domain.aggregates.model.TipoProducto;
import com.gestion.sgc.domain.ports.inputs.TipoProductoIn;
import com.gestion.sgc.domain.ports.outputs.TipoProductoRepositoryPort;
import com.gestion.sgc.infraestructure.mapper.TipoProductoMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TipoProductoUseCase implements TipoProductoIn {

    private final TipoProductoRepositoryPort tipoProductoRepository;
    private final TipoProductoMapper tipoProductoMapper;

    @Override
    @Transactional
    public TipoProductoResponse createTipoProducto(TipoProductoRequest request) {
        // Validar que no exista tipo con el mismo nombre
        if (tipoProductoRepository.existsByNombre(request.getNombre())) {
            throw new RuntimeException("Ya existe un tipo de producto con el nombre: " + request.getNombre());
        }

        // Crear tipo de producto
        TipoProducto tipoProducto = tipoProductoMapper.toDomainFromRequest(request);
        TipoProducto tipoGuardado = tipoProductoRepository.save(tipoProducto);

        return tipoProductoMapper.toResponse(tipoGuardado);
    }

    @Override
    public Optional<TipoProductoResponse> findById(Long id) {
        return tipoProductoRepository.findById(id)
                .map(tipoProductoMapper::toResponse);
    }

    @Override
    public List<TipoProductoResponse> getAllTipoProductos() {
        return tipoProductoMapper.toResponseList(tipoProductoRepository.findAll());
    }

    @Override
    public Optional<TipoProductoResponse> findByNombre(String nombre) {
        return tipoProductoRepository.findByNombre(nombre)
                .map(tipoProductoMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteTipoProducto(Long id) {
        // Verificar si existen productos usando este tipo antes de eliminar
        if (tipoProductoRepository.existsById(id)) {
            tipoProductoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Tipo de producto no encontrado con ID: " + id);
        }
    }


}
