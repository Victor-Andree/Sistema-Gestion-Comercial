package com.gestion.sgc.domain.ports.inputs;

import com.gestion.sgc.application.dto.response.DevolucionCajaPendienteResponse;
import java.util.List;

public interface DevolucionCajaIn {
    List<DevolucionCajaPendienteResponse> listarPendientes();
}
