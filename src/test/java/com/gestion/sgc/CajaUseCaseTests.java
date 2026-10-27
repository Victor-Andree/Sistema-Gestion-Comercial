package com.gestion.sgc;

import com.gestion.sgc.application.useCase.CajaUseCase;
import com.gestion.sgc.domain.aggregates.constans.EstadoSesionCaja;
import com.gestion.sgc.domain.aggregates.model.Caja;
import com.gestion.sgc.domain.aggregates.model.SesionCaja;
import com.gestion.sgc.domain.aggregates.model.Usuario;
import com.gestion.sgc.domain.ports.outputs.CajaRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.SesionCajaRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.auth.UsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CajaUseCaseTests {
    CajaRepositoryPort cajas;
    SesionCajaRepositoryPort sesiones;
    UsuarioRepositoryPort usuarios;
    CajaUseCase useCase;

    @BeforeEach
    void setUp() {
        cajas = mock(CajaRepositoryPort.class);
        sesiones = mock(SesionCajaRepositoryPort.class);
        usuarios = mock(UsuarioRepositoryPort.class);
        useCase = new CajaUseCase(cajas, sesiones, usuarios);
    }

    @Test
    void opensWithZeroAndAuthenticatedUser() {
        Caja caja = new Caja(1L, "Principal", true);
        Usuario usuario = new Usuario(); usuario.setUsuarioId(7L); usuario.setUsername("admin");
        when(cajas.findByIdForUpdate(1L)).thenReturn(Optional.of(caja));
        when(sesiones.findOpenByCajaId(1L)).thenReturn(Optional.empty());
        when(usuarios.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(sesiones.save(any())).thenAnswer(invocation -> {
            SesionCaja saved = invocation.getArgument(0); saved.setSesionCajaId(10L); return saved;
        });

        var response = useCase.abrir(1L, BigDecimal.ZERO, "admin");

        assertEquals(new BigDecimal("0.00"), response.montoInicial());
        assertEquals("admin", response.usuarioApertura());
        assertEquals("ABIERTA", response.estado());
    }

    @Test
    void opensWithPositiveAmount() {
        Caja caja = new Caja(1L, "Principal", true);
        Usuario usuario = new Usuario(); usuario.setUsuarioId(7L); usuario.setUsername("admin");
        when(cajas.findByIdForUpdate(1L)).thenReturn(Optional.of(caja));
        when(sesiones.findOpenByCajaId(1L)).thenReturn(Optional.empty());
        when(usuarios.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(sesiones.save(any())).thenAnswer(invocation -> {
            SesionCaja saved = invocation.getArgument(0); saved.setSesionCajaId(11L); return saved;
        });
        assertEquals(new BigDecimal("125.50"), useCase.abrir(1L, new BigDecimal("125.50"), "admin").montoInicial());
    }

    @Test
    void rejectsNegativeAmountBeforePersistence() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> useCase.abrir(1L, new BigDecimal("-0.01"), "admin"));
        assertEquals(400, error.getStatusCode().value());
        verify(cajas, never()).findByIdForUpdate(any());
    }

    @Test
    void rejectsMoreThanTwoDecimalPlacesBeforePersistence() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> useCase.abrir(1L, new BigDecimal("10.001"), "admin"));
        assertEquals(400, error.getStatusCode().value());
        verify(cajas, never()).findByIdForUpdate(any());
    }

    @Test
    void rejectsAmountOutsideDecimalRangeBeforePersistence() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> useCase.abrir(1L, new BigDecimal("10000000000000.00"), "admin"));
        assertEquals(400, error.getStatusCode().value());
        verify(cajas, never()).findByIdForUpdate(any());
    }

    @Test
    void rejectsDuplicateOpening() {
        Caja caja = new Caja(1L, "Principal", true);
        when(cajas.findByIdForUpdate(1L)).thenReturn(Optional.of(caja));
        when(sesiones.findOpenByCajaId(1L)).thenReturn(Optional.of(
                new SesionCaja(2L, caja, EstadoSesionCaja.ABIERTA, BigDecimal.ZERO,
                        LocalDateTime.now(), new Usuario())));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> useCase.abrir(1L, BigDecimal.ZERO, "admin"));
        assertEquals(409, error.getStatusCode().value());
        verify(sesiones, never()).save(any());
    }

    @Test
    void currentSessionMissingReturns404() {
        when(sesiones.findOpenByCajaId(1L)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> useCase.obtenerSesionActual(1L)).getStatusCode().value());
    }
}
