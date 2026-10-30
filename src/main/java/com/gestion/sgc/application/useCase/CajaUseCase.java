package com.gestion.sgc.application.useCase;

import com.gestion.sgc.application.dto.response.CajaResponse;
import com.gestion.sgc.application.dto.response.SesionCajaResponse;
import com.gestion.sgc.application.dto.request.CierreCajaRequest;
import com.gestion.sgc.domain.aggregates.constans.EstadoSesionCaja;
import com.gestion.sgc.domain.aggregates.model.Caja;
import com.gestion.sgc.domain.aggregates.model.SesionCaja;
import com.gestion.sgc.domain.aggregates.model.Usuario;
import com.gestion.sgc.domain.ports.inputs.CajaIn;
import com.gestion.sgc.domain.ports.outputs.CajaRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.SesionCajaRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.auth.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CajaUseCase implements CajaIn {
    private static final BigDecimal MAX_MONTO_INICIAL = new BigDecimal("9999999999999.99");
    private final CajaRepositoryPort cajas;
    private final SesionCajaRepositoryPort sesiones;
    private final UsuarioRepositoryPort usuarios;
    private final SaldoCajaCalculator saldoCalculator;

    @Override
    @Transactional(readOnly = true)
    public List<CajaResponse> listar() {
        return cajas.findAll().stream()
                .map(caja -> new CajaResponse(caja.getCajaId(), caja.getNombre(), caja.isActiva(),
                        sesiones.findOpenByCajaId(caja.getCajaId()).isPresent()))
                .toList();
    }

    @Override
    @Transactional
    public SesionCajaResponse abrir(Long cajaId, BigDecimal montoInicial, String username) {
        if (montoInicial == null || montoInicial.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto inicial no puede ser negativo");
        }
        if (montoInicial.scale() > 2 || montoInicial.compareTo(MAX_MONTO_INICIAL) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El monto inicial admite hasta 13 enteros y 2 decimales");
        }

        Caja caja = cajas.findByIdForUpdate(cajaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caja no encontrada"));
        if (!caja.isActiva()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La caja está inactiva");
        }
        if (sesiones.findOpenByCajaId(cajaId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La caja ya tiene una sesión abierta");
        }

        Usuario responsable = usuarios.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario autenticado no encontrado"));
        SesionCaja sesion = new SesionCaja(null, caja, EstadoSesionCaja.ABIERTA,
                montoInicial.setScale(2, RoundingMode.UNNECESSARY), LocalDateTime.now(), responsable);
        try {
            return response(sesiones.save(sesion));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La caja ya tiene una sesión abierta", exception);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SesionCajaResponse obtenerSesionActual(Long cajaId) {
        return sesiones.findOpenByCajaId(cajaId)
                .map(this::response)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La caja no tiene una sesión abierta"));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SesionCajaResponse cerrar(Long sesionId, CierreCajaRequest request, String username) {
        SesionCaja sesion = sesiones.findByIdForUpdate(sesionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión de caja no encontrada"));
        if (sesion.getEstado() != EstadoSesionCaja.ABIERTA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sesión de caja ya está cerrada");
        }
        validarMontoContado(request.montoContado());
        Usuario responsable = usuarios.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Usuario autenticado no encontrado"));
        BigDecimal esperado = saldoCalculator.saldo(sesion).setScale(2, RoundingMode.UNNECESSARY);
        BigDecimal contado = request.montoContado().setScale(2, RoundingMode.UNNECESSARY);

        sesion.setSaldoEsperado(esperado);
        sesion.setMontoContado(contado);
        sesion.setDiferencia(contado.subtract(esperado));
        sesion.setFechaCierre(LocalDateTime.now());
        sesion.setUsuarioCierre(responsable);
        String observacion = request.observacion() == null ? null : request.observacion().trim();
        sesion.setObservacionCierre(observacion == null || observacion.isEmpty() ? null : observacion);
        sesion.setEstado(EstadoSesionCaja.CERRADA);
        return response(sesiones.save(sesion));
    }

    private void validarMontoContado(BigDecimal monto) {
        if (monto == null || monto.signum() < 0 || monto.scale() > 2 || monto.compareTo(MAX_MONTO_INICIAL) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El monto contado debe ser no negativo y admite hasta 13 enteros y 2 decimales");
        }
    }

    private SesionCajaResponse response(SesionCaja sesion) {
        Usuario usuario = sesion.getUsuarioApertura();
        Usuario cierre = sesion.getUsuarioCierre();
        return new SesionCajaResponse(sesion.getSesionCajaId(), sesion.getCaja().getCajaId(),
                sesion.getCaja().getNombre(), sesion.getEstado().name(), sesion.getMontoInicial(),
                sesion.getFechaApertura(), usuario.getUsuarioId(), usuario.getUsername(),
                sesion.getSaldoEsperado(), sesion.getMontoContado(), sesion.getDiferencia(),
                sesion.getFechaCierre(), cierre == null ? null : cierre.getUsuarioId(),
                cierre == null ? null : cierre.getUsername(), sesion.getObservacionCierre());
    }
}
