package com.gestion.sgc.application.useCase;

import com.gestion.sgc.application.dto.request.DetalleVentaRequest;
import com.gestion.sgc.application.dto.request.VentaRequest;
import com.gestion.sgc.application.dto.response.VentaResponse;
import com.gestion.sgc.domain.aggregates.constans.EstadoComprobante;
import com.gestion.sgc.domain.aggregates.constans.EstadoVenta;
import com.gestion.sgc.domain.aggregates.constans.TipoComprobante;
import com.gestion.sgc.domain.aggregates.model.*;
import com.gestion.sgc.domain.ports.inputs.VentaIn;
import com.gestion.sgc.domain.ports.outputs.ClienteRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.ProductoRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.StockRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.VentaRepositoryPort;
import com.gestion.sgc.domain.ports.outputs.auth.UsuarioRepositoryPort;
import com.gestion.sgc.infraestructure.mapper.VentaMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    @Override
    @Transactional
    public VentaResponse registrarVenta(VentaRequest request) {
        // 1. Obtener usuario actual (vendedor)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Obtener cliente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // 3. Crear venta
        Venta venta = ventaMapper.toDomainFromRequest(request);
        venta.setUsuario(usuario);
        venta.setCliente(cliente);
        venta.setFechaVenta(LocalDateTime.now());
        venta.setEstado(EstadoVenta.COMPLETADA);

        double totalVenta = 0.0;
        List<DetalleVenta> detalles = new ArrayList<>();

        // 4. Procesar cada detalle
        for (DetalleVentaRequest detalleReq : request.getDetalles()) {
            // Obtener producto
            Producto producto = productoRepository.findById(detalleReq.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detalleReq.getProductoId()));

            // Verificar stock
            Optional<Stock> stockOpt = stockRepository.findByProductoId(detalleReq.getProductoId());
            if (stockOpt.isEmpty() || stockOpt.get().getCantidadActual() < detalleReq.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            // Calcular subtotal
            double subtotal = producto.getPrecioVenta() * detalleReq.getCantidad();
            totalVenta += subtotal;

            // Crear detalle
            DetalleVenta detalle = ventaMapper.toDomainFromRequest(detalleReq);
            detalle.setProducto(producto);
            detalle.setVenta(venta);
            detalle.setCantidad(detalleReq.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setSubtotal(subtotal);

            detalles.add(detalle);

            // Actualizar stock
            Stock stock = stockOpt.get();
            stock.setCantidadActual(stock.getCantidadActual() - detalleReq.getCantidad());
            stock.setFechaActualizacion(LocalDateTime.now());
            stockRepository.save(stock);
        }

        // 5. Guardar venta
        venta.setTotal(totalVenta);
        Venta ventaGuardada = ventaRepository.save(venta);

        // 6. Guardar detalles
        for (DetalleVenta detalle : detalles) {
            detalle.setVenta(ventaGuardada);
            ventaRepository.saveDetalle(detalle);
        }

        // 7. Crear comprobante
        Comprobante comprobante = crearComprobante(ventaGuardada, request.getTipoComprobante());
        ventaGuardada.setComprobante(comprobante);

        log.info("Venta registrada: ID={}, Total=S/{}, Cliente={}",
                ventaGuardada.getVentaId(), totalVenta, cliente.getPersona().getNombre());

        return buildVentaResponse(ventaGuardada, detalles);
    }

    @Override
    public Optional<VentaResponse> buscarPorId(Long id) {
        return ventaRepository.findById(id)
                .map(venta -> {
                    List<DetalleVenta> detalles = ventaRepository.findDetallesByVentaId(id);
                    return buildVentaResponse(venta, detalles);
                });
    }

    @Override
    public List<VentaResponse> listarPorFecha(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        return ventaRepository.findByFechaBetween(inicio, fin).stream()
                .map(venta -> {
                    List<DetalleVenta> detalles = ventaRepository.findDetallesByVentaId(venta.getVentaId());
                    return buildVentaResponse(venta, detalles);
                })
                .toList();
    }

    @Override
    public List<VentaResponse> listarPorCliente(Long clienteId) {
        return ventaRepository.findByClienteId(clienteId).stream()
                .map(venta -> {
                    List<DetalleVenta> detalles = ventaRepository.findDetallesByVentaId(venta.getVentaId());
                    return buildVentaResponse(venta, detalles);
                })
                .toList();
    }

    @Override
    public List<VentaResponse> listarPorUsuario(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId).stream()
                .map(venta -> {
                    List<DetalleVenta> detalles = ventaRepository.findDetallesByVentaId(venta.getVentaId());
                    return buildVentaResponse(venta, detalles);
                })
                .toList();
    }

    @Override
    public List<VentaResponse> listarTodas() {
        return ventaRepository.findAll().stream()
                .map(venta -> {
                    List<DetalleVenta> detalles = ventaRepository.findDetallesByVentaId(venta.getVentaId());
                    return buildVentaResponse(venta, detalles);
                })
                .toList();
    }

    @Override
    @Transactional
    public VentaResponse anularVenta(Long id, String motivo) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + id));

        if (!"COMPLETADA".equals(venta.getEstado())) {
            throw new RuntimeException("La venta ya está anulada");
        }

        venta.setEstado(EstadoVenta.ANULADA);

        // Revertir stock (devolver productos)
        List<DetalleVenta> detalles = ventaRepository.findDetallesByVentaId(id);
        for (DetalleVenta detalle : detalles) {
            Optional<Stock> stockOpt = stockRepository.findByProductoId(detalle.getProducto().getProductoId());
            stockOpt.ifPresent(stock -> {
                stock.setCantidadActual(stock.getCantidadActual() + detalle.getCantidad());
                stock.setFechaActualizacion(LocalDateTime.now());
                stockRepository.save(stock);
            });
        }

        // Anular comprobante
        Optional<Comprobante> comprobanteOpt = ventaRepository.findComprobanteByVentaId(id);
        comprobanteOpt.ifPresent(comp -> {
            comp.setEstado(EstadoComprobante.ANULADO);
            ventaRepository.saveComprobante(comp);
        });

        Venta ventaAnulada = ventaRepository.save(venta);
        log.info("Venta anulada: ID={}, Motivo={}", id, motivo);

        return buildVentaResponse(ventaAnulada, detalles);
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

        return ventaRepository.findByFechaBetween(inicio, fin).stream()
                .map(venta -> {
                    List<DetalleVenta> detalles = ventaRepository.findDetallesByVentaId(venta.getVentaId());
                    return buildVentaResponse(venta, detalles);
                })
                .toList();
    }

    // ===== MÉTODOS PRIVADOS =====

    private Comprobante crearComprobante(Venta venta, String tipoComprobante) {
        Comprobante comprobante = new Comprobante();
        comprobante.setVenta(venta);

        comprobante.setTipo(
                tipoComprobante != null && !tipoComprobante.isEmpty()
                        ? TipoComprobante.valueOf(tipoComprobante.toUpperCase())
                        : TipoComprobante.BOLETA
        );


        comprobante.setCorrelativo(generarCorrelativo());
        comprobante.setEstado(EstadoComprobante.EMITIDO );
        comprobante.setFechaEmision(LocalDateTime.now());

        return ventaRepository.saveComprobante(comprobante);
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
        response.setDetalles(ventaMapper.toDetalleResponseList(detalles));

        ventaRepository.findComprobanteByVentaId(venta.getVentaId())
                .ifPresent(comp -> response.setComprobante(ventaMapper.toResponse(comp)));

        return response;
    }



}
