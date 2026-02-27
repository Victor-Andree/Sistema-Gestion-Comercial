package com.gestion.sgc.domain.ports.inputs;

import com.gestion.sgc.application.dto.request.VentaRequest;
import com.gestion.sgc.application.dto.response.VentaResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VentaIn {

    VentaResponse registrarVenta(VentaRequest request);

    Optional<VentaResponse> buscarPorId(Long id);

    List<VentaResponse> listarPorFecha(LocalDate fecha);

    List<VentaResponse> listarPorCliente(Long clienteId);

    List<VentaResponse> listarPorUsuario(Long usuarioId);

    List<VentaResponse> listarTodas();

    VentaResponse anularVenta(Long id, String motivo);

    Double obtenerTotalVentasDelDia();

    List<VentaResponse> listarPorRangoFechas(LocalDate desde, LocalDate hasta);


}
