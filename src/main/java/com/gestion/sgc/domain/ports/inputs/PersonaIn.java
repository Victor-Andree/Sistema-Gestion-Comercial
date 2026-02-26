package com.gestion.sgc.domain.ports.inputs;

import com.gestion.sgc.application.dto.request.PersonaRequest;
import com.gestion.sgc.application.dto.response.PersonaResponse;

import java.util.List;
import java.util.Optional;

public interface PersonaIn {

    PersonaResponse crearPersona(PersonaRequest request);
    Optional<PersonaResponse> buscarPorId(Long id);
    List<PersonaResponse> listarTodas();

}
