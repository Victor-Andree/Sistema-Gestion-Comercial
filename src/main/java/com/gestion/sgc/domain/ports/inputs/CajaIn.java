package com.gestion.sgc.domain.ports.inputs;

import com.gestion.sgc.application.dto.response.CajaResponse;
import com.gestion.sgc.application.dto.response.SesionCajaResponse;
import com.gestion.sgc.application.dto.request.CierreCajaRequest;

import java.math.BigDecimal;
import java.util.List;

public interface CajaIn {
    List<CajaResponse> listar();
    SesionCajaResponse abrir(Long cajaId, BigDecimal montoInicial, String username);
    SesionCajaResponse obtenerSesionActual(Long cajaId);
    SesionCajaResponse cerrar(Long sesionId, CierreCajaRequest request, String username);
}
