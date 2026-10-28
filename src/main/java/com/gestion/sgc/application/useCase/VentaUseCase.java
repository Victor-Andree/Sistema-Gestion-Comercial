package com.gestion.sgc.application.useCase;

import com.gestion.sgc.application.dto.request.DetalleVentaRequest;
import com.gestion.sgc.application.dto.request.VentaRequest;
import com.gestion.sgc.application.dto.request.CobroVentaRequest;
import com.gestion.sgc.application.dto.response.VentaResponse;
import com.gestion.sgc.domain.aggregates.constans.EstadoComprobante;
import com.gestion.sgc.domain.aggregates.constans.EstadoVenta;
import com.gestion.sgc.domain.aggregates.constans.TipoComprobante;
import com.gestion.sgc.domain.aggregates.constans.MetodoPago;
import com.gestion.sgc.domain.aggregates.constans.OrigenMovimientoCaja;
import com.gestion.sgc.domain.aggregates.constans.TipoMovimientoCaja;
import com.gestion.sgc.domain.aggregates.constans.EstadoSesionCaja;
import com.gestion.sgc.domain.aggregates.constans.EstadoDevolucionCaja;
import com.gestion.sgc.domain.aggregates.model.*;
import com.gestion.sgc.domain.ports.inputs.VentaIn;
import com.gestion.sgc.domain.ports.outputs.ClienteRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.ProductoRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.StockRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.VentaRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.MovimientoCajaRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.SesionCajaRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.DevolucionCajaPendienteRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.auth.UsuarioRepositoryPort;
import com.gestion.sgc.infraestructure.mapper.ComprobanteMapper;
import com.gestion.sgc.infraestructure.mapper.DetalleVentaMapper;
import com.gestion.sgc.infraestructure.mapper.VentaMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaUseCase implements VentaIn {

    private final VentaRepositoryPort ventaRepository;
    private final ProductoRepositoryPort productoRepository;
    private final ClienteRepositoryPort clienteRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final StockRepositoryPort stockRepository;
    private final VentaMapper ventaMapper;
    private final DetalleVentaMapper detalleVentaMapper;
    private final ComprobanteMapper comprobanteMapper;
    private final MovimientoCajaRepositoryPort movimientoCajaRepository;
    private final SesionCajaRepositoryPort sesionCajaRepository;
    private final DevolucionCajaPendienteRepositoryPort devolucionCajaRepository;

    @Override
    @Transactional
    public VentaResponse registrarVenta(VentaRequest request) {

        validarCobro(request.getMetodoPago(), request.getCajaId());

        // 1. Usuario actual
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Cliente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // 3. Crear venta
        Venta venta = ventaMapper.toDomainFromRequest(request);
        venta.setUsuario(usuario);
        venta.setCliente(cliente);
        venta.setFechaVenta(LocalDateTime.now());
        venta.setEstado(EstadoVenta.COMPLETADA);
        venta.setMetodoPago(request.getMetodoPago());

        BigDecimal totalVenta = BigDecimal.ZERO.setScale(2);
        List<DetalleVenta> detalles = new ArrayList<>();

        // 4. Procesar detalles
        for (DetalleVentaRequest detalleReq : request.getDetalles()) {

            Producto producto = productoRepository.findById(detalleReq.getProductoId())
                    .orElseThrow(() -> new RuntimeException(
                            "Producto no encontrado: " + detalleReq.getProductoId()));

            Stock stock = stockRepository.findByProductoId(detalleReq.getProductoId())
                    .orElseThrow(() -> new RuntimeException(
                            "Stock no encontrado para: " + producto.getNombre()));

            if (stock.getCantidadActual() < detalleReq.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            // Calcular subtotal
            BigDecimal subtotalMonetario = importe(producto.getPrecioVenta())
                    .multiply(BigDecimal.valueOf(detalleReq.getCantidad())).setScale(2, RoundingMode.HALF_UP);
            double subtotal = subtotalMonetario.doubleValue();
            totalVenta = totalVenta.add(subtotalMonetario);

            // Crear detalle
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(detalleReq.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setSubtotal(subtotal);

            detalles.add(detalle);

            // Actualizar stock
            stock.setCantidadActual(stock.getCantidadActual() - detalleReq.getCantidad());
            stock.setFechaActualizacion(LocalDateTime.now());
            stockRepository.save(stock);
        }

        // 5. Asignar total y detalles
        venta.setTotal(totalVenta.doubleValue());
        venta.setDetalles(detalles);

        // 6. Guardar TODO en cascada
        Venta ventaGuardada = ventaRepository.save(venta);

        registrarIngresoVentaSiCorresponde(ventaGuardada, request.getCajaId(), usuario);

        // 7. Crear comprobante
        Comprobante comprobante = crearComprobante(ventaGuardada, request.getTipoComprobante());
        ventaGuardada.setComprobante(comprobante);

        log.info("Venta registrada: ID={}, Total=S/{}, Cliente={}",
                ventaGuardada.getVentaId(),
                totalVenta,
                cliente.getPersona().getNombre());

        return buildVentaResponse(ventaGuardada, detalles);
    }

    @Override
    @Transactional
    public VentaResponse registrarVentaPendiente(VentaRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Venta venta = ventaMapper.toDomainFromRequest(request);
        venta.setUsuario(usuario);
        venta.setCliente(cliente);
        venta.setFechaVenta(LocalDateTime.now());
        venta.setEstado(EstadoVenta.PENDIENTE);
        venta.setMetodoPago(MetodoPago.NO_REGISTRADO);

        BigDecimal totalVenta = BigDecimal.ZERO.setScale(2);
        List<DetalleVenta> detalles = new ArrayList<>();

        for (DetalleVentaRequest detalleReq : request.getDetalles()) {

            Producto producto = productoRepository.findById(detalleReq.getProductoId())
                    .orElseThrow(() -> new RuntimeException(
                            "Producto no encontrado: " + detalleReq.getProductoId()));

            BigDecimal subtotalMonetario = importe(producto.getPrecioVenta())
                    .multiply(BigDecimal.valueOf(detalleReq.getCantidad())).setScale(2, RoundingMode.HALF_UP);
            double subtotal = subtotalMonetario.doubleValue();
            totalVenta = totalVenta.add(subtotalMonetario);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(detalleReq.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setSubtotal(subtotal);

            detalles.add(detalle);
        }

        venta.setTotal(totalVenta.doubleValue());
        venta.setDetalles(detalles);

        Venta ventaGuardada = ventaRepository.save(venta);

        log.info("Venta registrada como PENDIENTE: ID={}", ventaGuardada.getVentaId());

        return buildVentaResponse(ventaGuardada, detalles);
    }

    @Override
    @Transactional
    public VentaResponse confirmarVenta(Long id, CobroVentaRequest cobro) {

        validarCobro(cobro.metodoPago(), cobro.cajaId());
        Venta venta = ventaRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        validarAcceso(venta);

        if (venta.getEstado() == EstadoVenta.COMPLETADA) {
            validarReintentoConfirmacion(venta, cobro);
            return buildVentaResponse(venta, ventaRepository.findDetallesByVentaId(id));
        }
        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden confirmar ventas pendientes");
        }

        List<DetalleVenta> detalles = ventaRepository.findDetallesByVentaId(id);

        for (DetalleVenta detalle : detalles) {

            Stock stock = stockRepository
                    .findByProductoId(detalle.getProducto().getProductoId())
                    .orElseThrow(() -> new RuntimeException(
                            "Stock no encontrado para: " + detalle.getProducto().getNombre()));

            if (stock.getCantidadActual() < detalle.getCantidad()) {
                throw new RuntimeException(
                        "Stock insuficiente para: " + detalle.getProducto().getNombre());
            }

            stock.setCantidadActual(stock.getCantidadActual() - detalle.getCantidad());
            stock.setFechaActualizacion(LocalDateTime.now());
            stockRepository.save(stock);
        }

        venta.setEstado(EstadoVenta.COMPLETADA);
        venta.setMetodoPago(cobro.metodoPago());

        Comprobante comprobante = crearComprobante(venta, "BOLETA");
        venta.setComprobante(comprobante);

        Venta ventaConfirmada = ventaRepository.save(venta);
        registrarIngresoVentaSiCorresponde(ventaConfirmada, cobro.cajaId(), usuarioActual());
        log.info("Venta confirmada: ID={}", id);

        return buildVentaResponse(ventaConfirmada, detalles);
    }


    @Override
    public List<VentaResponse> listarPorFecha(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        return filtrarPorAcceso(ventaRepository.findByFechaBetween(inicio, fin)).stream()
                .map(venta -> {
                    List<DetalleVenta> detalles = ventaRepository.findDetallesByVentaId(venta.getVentaId());
                    return buildVentaResponse(venta, detalles);
                })
                .toList();
    }


    @Override
    public Optional<VentaResponse> buscarPorId(Long id) {
        return ventaRepository.findById(id)
                .filter(this::puedeAcceder)
                .map(venta -> {
                    List<DetalleVenta> detalles = ventaRepository.findDetallesByVentaId(id);
                    return buildVentaResponse(venta, detalles);
                });
    }




    @Override
    public List<VentaResponse> listarPorCliente(Long clienteId) {
        return filtrarPorAcceso(ventaRepository.findByClienteId(clienteId)).stream()
                .map(venta -> {
                    List<DetalleVenta> detalles = ventaRepository.findDetallesByVentaId(venta.getVentaId());
                    return buildVentaResponse(venta, detalles);
                })
                .toList();
    }

    @Override
    public List<VentaResponse> listarPorUsuario(Long usuarioId) {
        return filtrarPorAcceso(ventaRepository.findByUsuarioId(usuarioId)).stream()
                .map(venta -> {
                    List<DetalleVenta> detalles = ventaRepository.findDetallesByVentaId(venta.getVentaId());
                    return buildVentaResponse(venta, detalles);
                })
                .toList();
    }

    @Override
    public List<VentaResponse> listarTodas() {
        return filtrarPorAcceso(ventaRepository.findAll()).stream()
                .map(venta -> {
                    List<DetalleVenta> detalles = ventaRepository.findDetallesByVentaId(venta.getVentaId());
                    return buildVentaResponse(venta, detalles);
                })
                .toList();
    }

    @Override
    @Transactional
    public VentaResponse anularVenta(Long id, String motivo) {
        Venta venta = ventaRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + id));

        validarAcceso(venta);

        if (!esAdmin() && venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new IllegalStateException("El asistente solo puede anular ventas pendientes");
        }

        if (venta.getEstado() == EstadoVenta.ANULADA) {
            return buildVentaResponse(venta, ventaRepository.findDetallesByVentaId(id));
        }

        List<DetalleVenta> detalles = ventaRepository.findDetallesByVentaId(id);

        // Si estaba COMPLETADA entonces devolver stock
        if (venta.getEstado() == EstadoVenta.COMPLETADA) {

            registrarCompensacionSiCorresponde(venta, usuarioActual());

            for (DetalleVenta detalle : detalles) {
                stockRepository.findByProductoId(detalle.getProducto().getProductoId())
                        .ifPresent(stock -> {
                            stock.setCantidadActual(
                                    stock.getCantidadActual() + detalle.getCantidad());
                            stock.setFechaActualizacion(LocalDateTime.now());
                            stockRepository.save(stock);
                        });
            }

            // anular comprobante
            ventaRepository.findComprobanteByVentaId(id)
                    .ifPresent(comp -> {
                        comp.setEstado(EstadoComprobante.ANULADO);
                        ventaRepository.saveComprobante(comp);
                    });
        }

        // Si estaba PENDIENTE entonces NO tocar stock


        venta.setEstado(EstadoVenta.ANULADA);

        Venta ventaAnulada = ventaRepository.save(venta);

        log.info("Venta anulada: ID={}, Motivo={}", id, motivo);

        return buildVentaResponse(ventaAnulada, detalles);
    }

    @Override
    @Transactional
    public List<VentaResponse> listarVentasPendientes() {

        List<Venta> ventas = filtrarPorAcceso(
                ventaRepository.findByEstado(EstadoVenta.PENDIENTE));

        return ventas.stream()
                .map(venta -> {
                    List<DetalleVenta> detalles =
                            ventaRepository.findDetallesByVentaId(venta.getVentaId());
                    return buildVentaResponse(venta, detalles);
                })
                .toList();
    }

    @Override
    public Double obtenerTotalVentasDelDia() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = LocalDate.now().atTime(LocalTime.MAX);
        return ventaRepository.sumTotalVentasByFecha(inicio, fin);
    }

    @Override
    public List<VentaResponse> listarPorRangoFechas(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(LocalTime.MAX);

        return filtrarPorAcceso(ventaRepository.findByFechaBetween(inicio, fin)).stream()
                .map(venta -> {
                    List<DetalleVenta> detalles = ventaRepository.findDetallesByVentaId(venta.getVentaId());
                    return buildVentaResponse(venta, detalles);
                })
                .toList();
    }

    // MÉTODOS PRIVADOS

    private boolean esAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private Usuario usuarioActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));
    }

    private boolean puedeAcceder(Venta venta) {
        if (esAdmin()) return true;
        Long usuarioId = usuarioActual().getUsuarioId();
        return venta.getUsuario() != null
                && usuarioId.equals(venta.getUsuario().getUsuarioId());
    }

    private List<Venta> filtrarPorAcceso(List<Venta> ventas) {
        if (esAdmin()) return ventas;
        Long usuarioId = usuarioActual().getUsuarioId();
        return ventas.stream()
                .filter(venta -> venta.getUsuario() != null
                        && usuarioId.equals(venta.getUsuario().getUsuarioId()))
                .toList();
    }

    private void validarAcceso(Venta venta) {
        if (!puedeAcceder(venta)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "No puedes operar una venta registrada por otro usuario");
        }
    }

    private void validarCobro(MetodoPago metodoPago, Long cajaId) {
        if (metodoPago == null || metodoPago == MetodoPago.NO_REGISTRADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El método de pago es obligatorio");
        }
        if (metodoPago == MetodoPago.EFECTIVO && cajaId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debes seleccionar una caja para cobrar en efectivo");
        }
        if (metodoPago != MetodoPago.EFECTIVO && cajaId != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La caja solo debe enviarse para pagos en efectivo");
        }
    }

    private void validarReintentoConfirmacion(Venta venta, CobroVentaRequest cobro) {
        if (venta.getMetodoPago() != cobro.metodoPago()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La venta ya fue confirmada con otro método de pago");
        }
        if (venta.getMetodoPago() != MetodoPago.EFECTIVO) return;

        MovimientoCaja original = movimientoCajaRepository
                .findByIdempotencyKey("venta-" + venta.getVentaId() + "-cobro")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "La venta figura completada, pero su cobro en caja no existe"));
        boolean coincide = original.getOrigen() == OrigenMovimientoCaja.VENTA
                && original.getTipo() == TipoMovimientoCaja.INGRESO
                && venta.getVentaId().equals(original.getVentaId())
                && original.getSesionCaja() != null
                && original.getSesionCaja().getCaja() != null
                && cobro.cajaId() != null
                && cobro.cajaId().equals(original.getSesionCaja().getCaja().getCajaId());
        if (!coincide) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El reintento no corresponde al cobro y caja originales");
        }
    }

    private void registrarIngresoVentaSiCorresponde(Venta venta, Long cajaId, Usuario responsable) {
        if (venta.getMetodoPago() != MetodoPago.EFECTIVO) return;

        SesionCaja encontrada = sesionCajaRepository.findOpenByCajaId(cajaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "La caja seleccionada no tiene una sesión abierta"));
        SesionCaja sesion = sesionCajaRepository.findByIdForUpdate(encontrada.getSesionCajaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "La sesión de caja ya no está disponible"));
        if (sesion.getEstado() != EstadoSesionCaja.ABIERTA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sesión de caja está cerrada");
        }

        String key = "venta-" + venta.getVentaId() + "-cobro";
        if (movimientoCajaRepository.findByIdempotencyKey(key).isPresent()) return;

        BigDecimal monto = importe(venta.getTotal());
        MovimientoCaja movimiento = new MovimientoCaja(null, key, sesion, TipoMovimientoCaja.INGRESO,
                monto, "Cobro en efectivo de venta #" + venta.getVentaId(), LocalDateTime.now(),
                responsable, OrigenMovimientoCaja.VENTA, venta.getVentaId(), null);
        movimientoCajaRepository.save(movimiento);
    }

    private void registrarCompensacionSiCorresponde(Venta venta, Usuario responsable) {
        if (venta.getMetodoPago() != MetodoPago.EFECTIVO) return;

        String cobroKey = "venta-" + venta.getVentaId() + "-cobro";
        MovimientoCaja cobro = movimientoCajaRepository.findByIdempotencyKey(cobroKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "No se encontró el cobro en caja de la venta"));
        SesionCaja sesionOriginal = sesionCajaRepository.findByIdForUpdate(cobro.getSesionCaja().getSesionCajaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "La sesión del cobro no está disponible"));

        String compensationKey = "venta-" + venta.getVentaId() + "-anulacion";
        if (movimientoCajaRepository.findByIdempotencyKey(compensationKey).isPresent()) return;

        SesionCaja sesion = sesionOriginal;
        if (sesionOriginal.getEstado() != EstadoSesionCaja.ABIERTA) {
            Optional<SesionCaja> actual = sesionCajaRepository
                    .findOpenByCajaId(sesionOriginal.getCaja().getCajaId());
            if (actual.isEmpty()) {
                registrarDevolucionPendiente(venta, cobro, sesionOriginal.getCaja().getCajaId());
                return;
            }
            sesion = sesionCajaRepository.findByIdForUpdate(actual.get().getSesionCajaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                            "La sesión actual de caja ya no está disponible"));
        }

        BigDecimal ingresos = movimientoCajaRepository.sumBySesionIdAndTipo(
                sesion.getSesionCajaId(), TipoMovimientoCaja.INGRESO);
        BigDecimal egresos = movimientoCajaRepository.sumBySesionIdAndTipo(
                sesion.getSesionCajaId(), TipoMovimientoCaja.EGRESO);
        BigDecimal saldo = sesion.getMontoInicial()
                .add(ingresos == null ? BigDecimal.ZERO : ingresos)
                .subtract(egresos == null ? BigDecimal.ZERO : egresos);
        if (saldo.compareTo(cobro.getMonto()) < 0) {
            registrarDevolucionPendiente(venta, cobro, sesion.getCaja().getCajaId());
            return;
        }

        MovimientoCaja compensacion = new MovimientoCaja(null, compensationKey, sesion,
                TipoMovimientoCaja.EGRESO, cobro.getMonto(),
                "Anulación de cobro de venta #" + venta.getVentaId(), LocalDateTime.now(), responsable,
                OrigenMovimientoCaja.VENTA, venta.getVentaId(), cobro.getMovimientoCajaId());
        movimientoCajaRepository.save(compensacion);
    }

    private void registrarDevolucionPendiente(Venta venta, MovimientoCaja cobro, Long cajaId) {
        if (devolucionCajaRepository.findByVentaId(venta.getVentaId()).isPresent()) return;
        devolucionCajaRepository.save(new DevolucionCajaPendiente(null, venta.getVentaId(), cajaId,
                cobro.getMovimientoCajaId(), cobro.getMonto(), EstadoDevolucionCaja.PENDIENTE,
                LocalDateTime.now(), null));
    }

    private BigDecimal importe(Double value) {
        if (value == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Importe de venta inválido");
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private Comprobante crearComprobante(Venta venta, String tipoComprobante) {
        Comprobante comprobante = new Comprobante();
        comprobante.setVenta(venta);

        comprobante.setTipo(
                tipoComprobante != null && !tipoComprobante.isEmpty()
                        ? TipoComprobante.valueOf(tipoComprobante.toUpperCase())
                        : TipoComprobante.BOLETA
        );


        comprobante.setCorrelativo(generarCorrelativo());
        comprobante.setEstado(EstadoComprobante.EMITIDO);
        comprobante.setFechaEmision(LocalDateTime.now());
        comprobante.setVenta(venta);
        venta.setComprobante(comprobante);

        return comprobante;
    }

    private String generarCorrelativo() {
        // Formato: AÑO-MES-NÚMERO (ej: 2026-02-0001)
        String year = String.valueOf(LocalDate.now().getYear());
        String month = String.format("%02d", LocalDate.now().getMonthValue());
        String correlativo = year + "-" + month + "-" + String.format("%04d",
                (int)(Math.random() * 9999));

        // Verificar que no exista
        Optional<Comprobante> existente = ventaRepository.findComprobanteByCorrelativo(correlativo);
        if (existente.isPresent()) {
            return generarCorrelativo(); // Recursivo hasta encontrar uno único
        }
        return correlativo;
    }

    private VentaResponse buildVentaResponse(Venta venta, List<DetalleVenta> detalles) {

        VentaResponse response = ventaMapper.toResponse(venta);

        response.setDetalles(detalleVentaMapper.toResponseList(detalles));

            ventaRepository.findComprobanteByVentaId(venta.getVentaId())
                .ifPresent(comp ->
                        response.setComprobante(comprobanteMapper.toResponse(comp))
                );

        return response;
    }



}
