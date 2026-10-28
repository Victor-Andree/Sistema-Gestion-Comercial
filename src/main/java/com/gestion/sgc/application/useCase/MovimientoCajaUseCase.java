package com.gestion.sgc.application.useCase;

import com.gestion.sgc.application.dto.request.MovimientoCajaRequest;
import com.gestion.sgc.application.dto.response.MovimientoCajaResponse;
import com.gestion.sgc.application.dto.response.ResumenCajaResponse;
import com.gestion.sgc.domain.aggregates.constans.EstadoSesionCaja;
import com.gestion.sgc.domain.aggregates.constans.TipoMovimientoCaja;
import com.gestion.sgc.domain.aggregates.model.MovimientoCaja;
import com.gestion.sgc.domain.aggregates.model.SesionCaja;
import com.gestion.sgc.domain.aggregates.model.Usuario;
import com.gestion.sgc.domain.ports.inputs.MovimientoCajaIn;
import com.gestion.sgc.domain.ports.outputs.MovimientoCajaRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.SesionCajaRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.auth.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovimientoCajaUseCase implements MovimientoCajaIn {
    private static final BigDecimal MAX_MONTO = new BigDecimal("9999999999999.99");

    private final SesionCajaRepositoryPort sesiones;
    private final MovimientoCajaRepositoryPort movimientos;
    private final UsuarioRepositoryPort usuarios;

    @Override
    @Transactional
    public MovimientoCajaResponse registrar(Long sesionId, MovimientoCajaRequest request, String username,
                                             String idempotencyKey) {
        validarMonto(request.monto());
        String key = validarIdempotencyKey(idempotencyKey);
        String concepto = request.concepto() == null ? "" : request.concepto().trim();
        if (concepto.isEmpty() || concepto.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El concepto es obligatorio y admite hasta 200 caracteres");
        }

        SesionCaja sesion = sesiones.findByIdForUpdate(sesionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión de caja no encontrada"));
        BigDecimal monto = request.monto().setScale(2, RoundingMode.UNNECESSARY);
        var existente = movimientos.findByIdempotencyKey(key);
        if (existente.isPresent()) {
            MovimientoCaja movimiento = existente.get();
            if (!movimiento.getSesionCaja().getSesionCajaId().equals(sesionId)
                    || movimiento.getTipo() != request.tipo()
                    || movimiento.getMonto().compareTo(monto) != 0
                    || !movimiento.getConcepto().equals(concepto)
                    || !movimiento.getUsuarioResponsable().getUsername().equals(username)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La clave de idempotencia ya fue utilizada con otra operación");
            }
            return response(movimiento);
        }
        if (sesion.getEstado() != EstadoSesionCaja.ABIERTA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sesión de caja está cerrada");
        }
        if (request.tipo() == TipoMovimientoCaja.EGRESO && saldo(sesion).compareTo(monto) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Saldo insuficiente para registrar el egreso");
        }

        Usuario responsable = usuarios.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Usuario autenticado no encontrado"));
        MovimientoCaja movimiento = new MovimientoCaja(null, key, sesion, request.tipo(), monto, concepto,
                LocalDateTime.now(), responsable);
        return response(movimientos.save(movimiento));
    }

    private String validarIdempotencyKey(String value) {
        try {
            return UUID.fromString(value == null ? "" : value.trim()).toString();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Idempotency-Key debe ser un UUID válido");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoCajaResponse> listar(Long sesionId) {
        obtenerSesion(sesionId);
        return movimientos.findAllBySesionId(sesionId).stream().map(this::response).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenCajaResponse resumen(Long sesionId) {
        SesionCaja sesion = obtenerSesion(sesionId);
        BigDecimal ingresos = total(sesionId, TipoMovimientoCaja.INGRESO);
        BigDecimal egresos = total(sesionId, TipoMovimientoCaja.EGRESO);
        return new ResumenCajaResponse(sesionId, sesion.getMontoInicial(), ingresos, egresos,
                sesion.getMontoInicial().add(ingresos).subtract(egresos));
    }

    private SesionCaja obtenerSesion(Long sesionId) {
        return sesiones.findById(sesionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión de caja no encontrada"));
    }

    private BigDecimal saldo(SesionCaja sesion) {
        return sesion.getMontoInicial()
                .add(total(sesion.getSesionCajaId(), TipoMovimientoCaja.INGRESO))
                .subtract(total(sesion.getSesionCajaId(), TipoMovimientoCaja.EGRESO));
    }

    private BigDecimal total(Long sesionId, TipoMovimientoCaja tipo) {
        BigDecimal total = movimientos.sumBySesionIdAndTipo(sesionId, tipo);
        return (total == null ? BigDecimal.ZERO : total).setScale(2, RoundingMode.UNNECESSARY);
    }

    private void validarMonto(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto debe ser mayor que cero");
        }
        if (monto.scale() > 2 || monto.compareTo(MAX_MONTO) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El monto admite hasta 13 enteros y 2 decimales");
        }
    }

    private MovimientoCajaResponse response(MovimientoCaja movimiento) {
        return new MovimientoCajaResponse(movimiento.getMovimientoCajaId(),
                movimiento.getSesionCaja().getSesionCajaId(), movimiento.getTipo().name(),
                movimiento.getMonto(), movimiento.getConcepto(), movimiento.getFechaMovimiento(),
                movimiento.getUsuarioResponsable().getUsuarioId(),
                movimiento.getUsuarioResponsable().getUsername());
    }
}
