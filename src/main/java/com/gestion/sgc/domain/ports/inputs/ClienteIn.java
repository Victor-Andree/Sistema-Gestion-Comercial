package com.gestion.sgc.domain.ports.inputs;

import com.gestion.sgc.application.dto.request.ClienteRequest;
import com.gestion.sgc.application.dto.response.ClienteResponse;

import java.util.List;
import java.util.Optional;

public interface ClienteIn {

    ClienteResponse createCliente(ClienteRequest request);

    Optional<ClienteResponse> findById(Long id);

    List<ClienteResponse> getAllClientes();

    Optional<ClienteResponse> findByDni(String dni);

    void deleteCliente(Long id);

}
