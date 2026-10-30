package com.gestion.sgc;

import com.gestion.sgc.application.dto.request.MovimientoCajaRequest;
import com.gestion.sgc.application.useCase.MovimientoCajaUseCase;
import com.gestion.sgc.application.useCase.SaldoCajaCalculator;
import com.gestion.sgc.domain.aggregates.constans.EstadoSesionCaja;
import com.gestion.sgc.domain.aggregates.constans.TipoMovimientoCaja;
import com.gestion.sgc.domain.aggregates.constans.OrigenMovimientoCaja;
import com.gestion.sgc.domain.aggregates.model.Caja;
import com.gestion.sgc.domain.aggregates.model.MovimientoCaja;
import com.gestion.sgc.domain.aggregates.model.SesionCaja;
import com.gestion.sgc.domain.aggregates.model.Usuario;
import com.gestion.sgc.domain.ports.outputs.MovimientoCajaRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.SesionCajaRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.auth.UsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MovimientoCajaUseCaseTests {
    private static final String KEY = "11111111-1111-1111-1111-111111111111";
    SesionCajaRepositoryPort sesiones;
    MovimientoCajaRepositoryPort movimientos;
    UsuarioRepositoryPort usuarios;
    MovimientoCajaUseCase useCase;

    @BeforeEach
    void setUp() {
        sesiones = mock(SesionCajaRepositoryPort.class);
        movimientos = mock(MovimientoCajaRepositoryPort.class);
        usuarios = mock(UsuarioRepositoryPort.class);
        useCase = new MovimientoCajaUseCase(sesiones, movimientos, usuarios, new SaldoCajaCalculator(movimientos));
    }

    private SesionCaja sesion(EstadoSesionCaja estado, String monto) {
        return new SesionCaja(5L, new Caja(1L, "Principal", true), estado,
                new BigDecimal(monto), LocalDateTime.now(), new Usuario());
    }

    private void prepareOpenSession() {
        Usuario user = new Usuario(); user.setUsuarioId(9L); user.setUsername("admin");
        when(sesiones.findByIdForUpdate(5L)).thenReturn(Optional.of(sesion(EstadoSesionCaja.ABIERTA, "100.00")));
        when(usuarios.findByUsername("admin")).thenReturn(Optional.of(user));
        when(movimientos.sumBySesionIdAndTipo(any(), any())).thenReturn(new BigDecimal("0.00"));
        when(movimientos.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(movimientos.save(any())).thenAnswer(invocation -> {
            MovimientoCaja value = invocation.getArgument(0); value.setMovimientoCajaId(11L); return value;
        });
    }

    @Test void registersIncomeAndAuthenticatedUser() {
        prepareOpenSession();
        var result = useCase.registrar(5L,
                new MovimientoCajaRequest(TipoMovimientoCaja.INGRESO, new BigDecimal("25.50"), "  Fondo extra  "),
                "admin", KEY);
        assertEquals("INGRESO", result.tipo());
        assertEquals(new BigDecimal("25.50"), result.monto());
        assertEquals("Fondo extra", result.concepto());
        assertEquals("admin", result.usuarioResponsable());
    }

    @Test void registersExpenseWhenBalanceIsEnough() {
        prepareOpenSession();
        assertEquals("EGRESO", useCase.registrar(5L,
                new MovimientoCajaRequest(TipoMovimientoCaja.EGRESO, new BigDecimal("80.00"), "Compra"),
                "admin", KEY).tipo());
    }

    @Test void rejectsInvalidAmountsBeforeLocking() {
        for (String amount : List.of("0", "-1", "1.001", "10000000000000.00")) {
            assertEquals(400, assertThrows(ResponseStatusException.class, () -> useCase.registrar(5L,
                    new MovimientoCajaRequest(TipoMovimientoCaja.INGRESO, new BigDecimal(amount), "Concepto"),
                    "admin", KEY)).getStatusCode().value());
        }
        verify(sesiones, never()).findByIdForUpdate(any());
    }

    @Test void rejectsBlankAndLongConcept() {
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> useCase.registrar(5L,
                new MovimientoCajaRequest(TipoMovimientoCaja.INGRESO, BigDecimal.ONE, "   "), "admin", KEY))
                .getStatusCode().value());
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> useCase.registrar(5L,
                new MovimientoCajaRequest(TipoMovimientoCaja.INGRESO, BigDecimal.ONE, "x".repeat(201)), "admin", KEY))
                .getStatusCode().value());
    }

    @Test void rejectsMissingAndClosedSessions() {
        when(sesiones.findByIdForUpdate(5L)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> useCase.registrar(5L,
                new MovimientoCajaRequest(TipoMovimientoCaja.INGRESO, BigDecimal.ONE, "Concepto"), "admin", KEY))
                .getStatusCode().value());
        when(sesiones.findByIdForUpdate(5L)).thenReturn(Optional.of(sesion(EstadoSesionCaja.CERRADA, "10.00")));
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> useCase.registrar(5L,
                new MovimientoCajaRequest(TipoMovimientoCaja.INGRESO, BigDecimal.ONE, "Concepto"), "admin", KEY))
                .getStatusCode().value());
    }

    @Test void rejectsExpenseWithInsufficientBalance() {
        prepareOpenSession();
        when(movimientos.sumBySesionIdAndTipo(5L, TipoMovimientoCaja.INGRESO)).thenReturn(new BigDecimal("10.00"));
        when(movimientos.sumBySesionIdAndTipo(5L, TipoMovimientoCaja.EGRESO)).thenReturn(new BigDecimal("20.00"));
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> useCase.registrar(5L,
                new MovimientoCajaRequest(TipoMovimientoCaja.EGRESO, new BigDecimal("91.00"), "Compra"), "admin", KEY))
                .getStatusCode().value());
        verify(movimientos, never()).save(any());
    }

    @Test void calculatesProvisionalBalance() {
        when(sesiones.findById(5L)).thenReturn(Optional.of(sesion(EstadoSesionCaja.ABIERTA, "100.00")));
        when(movimientos.sumBySesionIdAndTipo(5L, TipoMovimientoCaja.INGRESO)).thenReturn(new BigDecimal("30.00"));
        when(movimientos.sumBySesionIdAndTipo(5L, TipoMovimientoCaja.EGRESO)).thenReturn(new BigDecimal("15.00"));
        var result = useCase.resumen(5L);
        assertEquals(new BigDecimal("115.00"), result.saldoProvisional());
    }

    @Test void repeatedIdempotencyKeyReturnsOriginalMovement() {
        prepareOpenSession();
        MovimientoCaja original = new MovimientoCaja(11L, KEY, sesion(EstadoSesionCaja.ABIERTA, "100.00"),
                TipoMovimientoCaja.INGRESO, new BigDecimal("25.50"), "Fondo extra",
                LocalDateTime.now(), new Usuario(), OrigenMovimientoCaja.MANUAL, null, null);
        original.getUsuarioResponsable().setUsuarioId(9L);
        original.getUsuarioResponsable().setUsername("admin");
        when(movimientos.findByIdempotencyKey(KEY)).thenReturn(Optional.of(original));

        var result = useCase.registrar(5L,
                new MovimientoCajaRequest(TipoMovimientoCaja.INGRESO, new BigDecimal("25.50"), "Fondo extra"),
                "admin", KEY);

        assertEquals(11L, result.movimientoCajaId());
        verify(movimientos, never()).save(any());
    }

    @Test void rejectsInvalidIdempotencyKey() {
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> useCase.registrar(5L,
                new MovimientoCajaRequest(TipoMovimientoCaja.INGRESO, BigDecimal.ONE, "Concepto"),
                "admin", "not-a-uuid")).getStatusCode().value());
    }
}
