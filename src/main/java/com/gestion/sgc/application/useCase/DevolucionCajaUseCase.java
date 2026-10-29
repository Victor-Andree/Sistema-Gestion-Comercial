package com.gestion.sgc.application.useCase;

import com.gestion.sgc.application.dto.response.DevolucionCajaPendienteResponse;
import com.gestion.sgc.domain.ports.inputs.DevolucionCajaIn;
import com.gestion.sgc.domain.ports.outputs.DevolucionCajaPendienteRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DevolucionCajaUseCase implements DevolucionCajaIn {
    private final DevolucionCajaPendienteRepositoryPort repository;

    @Override
    @Transactional(readOnly = true)
    public List<DevolucionCajaPendienteResponse> listarPendientes() {
        return repository.findAllPending().stream()
                .map(value -> new DevolucionCajaPendienteResponse(value.getDevolucionCajaId(),
                        value.getVentaId(), value.getCajaId(), value.getMovimientoCobroId(), value.getMonto(),
                        value.getEstado().name(), value.getFechaRegistro(), value.getMovimientoCompensacionId()))
                .toList();
    }
}
