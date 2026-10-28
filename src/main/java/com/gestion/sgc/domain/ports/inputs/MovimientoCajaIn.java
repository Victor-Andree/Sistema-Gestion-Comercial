package com.gestion.sgc.domain.ports.inputs;

import com.gestion.sgc.application.dto.request.MovimientoCajaRequest;
import com.gestion.sgc.application.dto.response.MovimientoCajaResponse;
import com.gestion.sgc.application.dto.response.ResumenCajaResponse;

import java.util.List;

public interface MovimientoCajaIn {
    MovimientoCajaResponse registrar(Long sesionId, MovimientoCajaRequest request, String username,
                                      String idempotencyKey);
    List<MovimientoCajaResponse> listar(Long sesionId);
    ResumenCajaResponse resumen(Long sesionId);
}
