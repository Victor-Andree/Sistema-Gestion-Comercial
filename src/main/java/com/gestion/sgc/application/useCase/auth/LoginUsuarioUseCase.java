package com.gestion.sgc.application.useCase.auth;

import com.gestion.sgc.application.dto.request.LoginRequest;
import com.gestion.sgc.application.dto.response.AuthenticactionResponse;
import com.gestion.sgc.domain.ports.inputs.auth.LoginUsuarioIn;
import com.gestion.sgc.domain.ports.outputs.auth.TokenPort;
import com.gestion.sgc.domain.ports.outputs.auth.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUsuarioUseCase implements LoginUsuarioIn {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepositoryPort usuarioRepository;
    private final TokenPort tokenPort;


    @Override
    public AuthenticactionResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = tokenPort.generateToken(usuario);
        String refreshToken = tokenPort.generateRefreshToken(usuario);

        return AuthenticactionResponse.builder()
                .token(token)
                .nombre(usuario.getPersona() != null ?
                        usuario.getPersona().getNombre() + " " + usuario.getPersona().getApellidos() :
                        usuario.getUsername())
                .tokenType("Bearer")
                .expiresIn(tokenPort.getExpirationTime() / 1000)
                .build();
    }

}
