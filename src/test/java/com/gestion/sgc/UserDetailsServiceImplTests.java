package com.gestion.sgc;

import com.gestion.sgc.domain.aggregates.model.Usuario;
import com.gestion.sgc.domain.ports.outputs.auth.UsuarioRepositoryPort;
import com.gestion.sgc.infraestructure.security.services.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailsServiceImplTests {

    @Test
    void activeUserCanAuthenticate() {
        UserDetailsServiceImpl service = serviceWith(usuario("activo", "ACTIVO"));

        UserDetails details = service.loadUserByUsername("activo");

        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
    }

    @Test
    void inactiveAndBlockedUsersCannotAuthenticate() {
        UserDetailsServiceImpl service = serviceWith(
                usuario("inactivo", "INACTIVO"),
                usuario("bloqueado", "BLOQUEADO")
        );

        assertThat(service.loadUserByUsername("inactivo").isEnabled()).isFalse();
        assertThat(service.loadUserByUsername("bloqueado").isEnabled()).isFalse();
        assertThat(service.loadUserByUsername("bloqueado").isAccountNonLocked()).isFalse();
    }

    private Usuario usuario(String username, String estado) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword("{noop}secret");
        usuario.setRol("ASISTENTE");
        usuario.setEstadoUsuario(estado);
        return usuario;
    }

    private UserDetailsServiceImpl serviceWith(Usuario... usuarios) {
        Map<String, Usuario> values = java.util.Arrays.stream(usuarios)
                .collect(java.util.stream.Collectors.toMap(Usuario::getUsername, usuario -> usuario));
        return new UserDetailsServiceImpl(new UsuarioRepositoryPort() {
            @Override public Optional<Usuario> findByUsername(String username) {
                return Optional.ofNullable(values.get(username));
            }
            @Override public Usuario save(Usuario usuario) { throw new UnsupportedOperationException(); }
            @Override public Optional<Usuario> findById(Long id) { return Optional.empty(); }
            @Override public boolean existsByUsername(String username) { return values.containsKey(username); }
            @Override public boolean existsByUsernameAndIdNot(String username, Long id) { return false; }
            @Override public List<Usuario> findAll() { return List.copyOf(values.values()); }
        });
    }
}
