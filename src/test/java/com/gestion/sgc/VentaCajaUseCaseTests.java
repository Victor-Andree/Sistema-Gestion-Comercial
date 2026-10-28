package com.gestion.sgc;

import com.gestion.sgc.application.dto.request.CobroVentaRequest;
import com.gestion.sgc.application.dto.request.DetalleVentaRequest;
import com.gestion.sgc.application.dto.request.VentaRequest;
import com.gestion.sgc.application.dto.response.VentaResponse;
import com.gestion.sgc.application.useCase.VentaUseCase;
import com.gestion.sgc.domain.aggregates.constans.*;
import com.gestion.sgc.domain.aggregates.model.*;
import com.gestion.sgc.domain.ports.outputs.*;
import com.gestion.sgc.domain.ports.outputs.auth.UsuarioRepositoryPort;
import com.gestion.sgc.infraestructure.mapper.ComprobanteMapper;
import com.gestion.sgc.infraestructure.mapper.DetalleVentaMapper;
import com.gestion.sgc.infraestructure.mapper.VentaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VentaCajaUseCaseTests {
    VentaRepositoryPort ventas = mock(VentaRepositoryPort.class);
    ProductoRepositoryPort productos = mock(ProductoRepositoryPort.class);
    ClienteRepositoryPort clientes = mock(ClienteRepositoryPort.class);
    UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
    StockRepositoryPort stocks = mock(StockRepositoryPort.class);
    VentaMapper ventaMapper = mock(VentaMapper.class);
    DetalleVentaMapper detalleMapper = mock(DetalleVentaMapper.class);
    ComprobanteMapper comprobanteMapper = mock(ComprobanteMapper.class);
    MovimientoCajaRepositoryPort movimientos = mock(MovimientoCajaRepositoryPort.class);
    SesionCajaRepositoryPort sesiones = mock(SesionCajaRepositoryPort.class);
    VentaUseCase useCase;
    Usuario admin;
    SesionCaja sesion;

    @BeforeEach void setUp() {
        useCase = new VentaUseCase(ventas, productos, clientes, usuarios, stocks, ventaMapper,
                detalleMapper, comprobanteMapper, movimientos, sesiones);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", "n/a", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        admin = new Usuario(); admin.setUsuarioId(1L); admin.setUsername("admin");
        when(usuarios.findByUsername("admin")).thenReturn(Optional.of(admin));

        Cliente cliente = new Cliente(); cliente.setClienteId(1L); cliente.setPersona(new Persona());
        cliente.getPersona().setNombre("Cliente");
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        Producto producto = new Producto(); producto.setProductoId(1L); producto.setNombre("Producto");
        producto.setPrecioVenta(25.0);
        when(productos.findById(1L)).thenReturn(Optional.of(producto));
        Stock stock = new Stock(); stock.setCantidadActual(10); stock.setProducto(producto);
        when(stocks.findByProductoId(1L)).thenReturn(Optional.of(stock));
        when(ventaMapper.toDomainFromRequest(any())).thenReturn(new Venta());
        when(ventas.save(any())).thenAnswer(invocation -> {
            Venta venta = invocation.getArgument(0);
            if (venta.getVentaId() == null) venta.setVentaId(10L);
            return venta;
        });
        when(ventas.findComprobanteByCorrelativo(anyString())).thenReturn(Optional.empty());
        when(ventas.findComprobanteByVentaId(anyLong())).thenReturn(Optional.empty());
        when(ventaMapper.toResponse(any())).thenAnswer(invocation -> {
            Venta venta = invocation.getArgument(0); VentaResponse response = new VentaResponse();
            response.setVentaId(venta.getVentaId()); response.setEstado(venta.getEstado().name());
            response.setMetodoPago(venta.getMetodoPago().name()); return response;
        });
        when(detalleMapper.toResponseList(anyList())).thenReturn(List.of());

        Caja caja = new Caja(1L, "Principal", true);
        sesion = new SesionCaja(2L, caja, EstadoSesionCaja.ABIERTA, new BigDecimal("100.00"),
                LocalDateTime.now(), admin);
        when(sesiones.findOpenByCajaId(1L)).thenReturn(Optional.of(sesion));
        when(sesiones.findByIdForUpdate(2L)).thenReturn(Optional.of(sesion));
        when(movimientos.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(movimientos.save(any())).thenAnswer(invocation -> {
            MovimientoCaja movement = invocation.getArgument(0); movement.setMovimientoCajaId(50L); return movement;
        });
    }

    private VentaRequest request(MetodoPago method, Long cashRegisterId) {
        VentaRequest request = new VentaRequest(); request.setClienteId(1L);
        request.setDetalles(List.of(new DetalleVentaRequest(1L, 2)));
        request.setMetodoPago(method); request.setCajaId(cashRegisterId); return request;
    }

    @Test void pendingSaleDoesNotAffectCash() {
        useCase.registrarVentaPendiente(request(null, null));
        verifyNoInteractions(movimientos);
    }

    @Test void cashSaleCreatesOneTraceableIncome() {
        useCase.registrarVenta(request(MetodoPago.EFECTIVO, 1L));
        verify(movimientos).save(argThat(m -> m.getOrigen() == OrigenMovimientoCaja.VENTA
                && m.getTipo() == TipoMovimientoCaja.INGRESO && m.getVentaId().equals(10L)
                && m.getMonto().equals(new BigDecimal("50.00"))));
    }

    @Test void yapeAndTransferDoNotAffectCash() {
        useCase.registrarVenta(request(MetodoPago.YAPE, null));
        useCase.registrarVenta(request(MetodoPago.TRANSFERENCIA, null));
        verifyNoInteractions(movimientos);
    }

    @Test void cashSaleWithoutOpenSessionIsRejected() {
        when(sesiones.findOpenByCajaId(1L)).thenReturn(Optional.empty());
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> useCase.registrarVenta(request(MetodoPago.EFECTIVO, 1L)));
        assertEquals(409, error.getStatusCode().value());
    }

    @Test void repeatedConfirmationDoesNotCreateAnotherIncome() {
        Venta completed = new Venta(); completed.setVentaId(10L); completed.setUsuario(admin);
        completed.setEstado(EstadoVenta.COMPLETADA); completed.setMetodoPago(MetodoPago.EFECTIVO);
        when(ventas.findByIdForUpdate(10L)).thenReturn(Optional.of(completed));
        when(ventas.findDetallesByVentaId(10L)).thenReturn(List.of());
        useCase.confirmarVenta(10L, new CobroVentaRequest(MetodoPago.EFECTIVO, 1L));
        verify(movimientos, never()).save(any());
    }

    @Test void cancellingCashSaleCreatesCompensation() {
        Venta completed = new Venta(); completed.setVentaId(10L); completed.setUsuario(admin);
        completed.setEstado(EstadoVenta.COMPLETADA); completed.setMetodoPago(MetodoPago.EFECTIVO);
        MovimientoCaja income = new MovimientoCaja(50L, "venta-10-cobro", sesion,
                TipoMovimientoCaja.INGRESO, new BigDecimal("50.00"), "Cobro", LocalDateTime.now(),
                admin, OrigenMovimientoCaja.VENTA, 10L, null);
        when(ventas.findByIdForUpdate(10L)).thenReturn(Optional.of(completed));
        when(ventas.findDetallesByVentaId(10L)).thenReturn(List.of());
        when(movimientos.findByIdempotencyKey("venta-10-cobro")).thenReturn(Optional.of(income));
        when(movimientos.findByIdempotencyKey("venta-10-anulacion")).thenReturn(Optional.empty());
        when(movimientos.sumBySesionIdAndTipo(2L, TipoMovimientoCaja.INGRESO)).thenReturn(new BigDecimal("50.00"));
        when(movimientos.sumBySesionIdAndTipo(2L, TipoMovimientoCaja.EGRESO)).thenReturn(BigDecimal.ZERO);

        useCase.anularVenta(10L, "Devolución");

        verify(movimientos).save(argThat(m -> m.getTipo() == TipoMovimientoCaja.EGRESO
                && m.getMovimientoOriginalId().equals(50L) && m.getVentaId().equals(10L)));
    }
}
