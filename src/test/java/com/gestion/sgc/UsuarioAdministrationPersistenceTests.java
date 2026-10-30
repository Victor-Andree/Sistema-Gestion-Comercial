package com.gestion.sgc;

import com.gestion.sgc.application.dto.request.CambiarEstadoUsuarioRequest;
import com.gestion.sgc.application.useCase.auth.AdministrarUsuarioUseCase;
import com.gestion.sgc.domain.aggregates.constans.EstadoUsuario;
import com.gestion.sgc.domain.aggregates.constans.RolEnum;
import com.gestion.sgc.domain.ports.inputs.auth.RegistrarUsuarioIn;
import com.gestion.sgc.domain.ports.outputs.PersonaRepositoryPort;
import com.gestion.sgc.infraestructure.adapters.auth.UsuarioRepositoryAdapter;
import com.gestion.sgc.infraestructure.entity.PersonaEntity;
import com.gestion.sgc.infraestructure.entity.UsuarioEntity;
import com.gestion.sgc.infraestructure.mapper.UsuarioMapperImpl;
import com.gestion.sgc.infraestructure.repository.PersonaJpaRepository;
import com.gestion.sgc.infraestructure.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(bootstrapMode = org.springframework.data.repository.config.BootstrapMode.LAZY, properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "logging.level.org.hibernate.SQL=WARN"
})
@Import({AdministrarUsuarioUseCase.class, UsuarioRepositoryAdapter.class, UsuarioMapperImpl.class})
class UsuarioAdministrationPersistenceTests {
    @Autowired AdministrarUsuarioUseCase useCase;
    @Autowired UsuarioJpaRepository usuarios;
    @Autowired PersonaJpaRepository personas;
    @MockitoBean PersonaRepositoryPort personaPort;
    @MockitoBean RegistrarUsuarioIn registrarUsuario;
    @MockitoBean PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanDatabase() {
        usuarios.deleteAll();
        personas.deleteAll();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentDeactivationsAlwaysLeaveOneActiveAdmin() throws Exception {
        Long firstId = seedAdmin("admin-one", "71000001");
        Long secondId = seedAdmin("admin-two", "71000002");
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> deactivateFirst = () -> deactivate(firstId, start);
        Callable<Integer> deactivateSecond = () -> deactivate(secondId, start);

        List<Integer> statuses;
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(deactivateFirst);
            var second = executor.submit(deactivateSecond);
            start.countDown();
            statuses = List.of(first.get(), second.get());
        }

        assertTrue(statuses.contains(200));
        assertTrue(statuses.contains(409));
        assertEquals(1, usuarios.findAll().stream()
                .filter(user -> user.getRol() == RolEnum.ADMIN)
                .filter(user -> user.getEstadoUsuario() == EstadoUsuario.ACTIVO)
                .count());
    }

    private int deactivate(Long id, CountDownLatch start) throws InterruptedException {
        start.await();
        try {
            useCase.cambiarEstado(id, new CambiarEstadoUsuarioRequest("INACTIVO"), "qa-admin");
            return 200;
        } catch (ResponseStatusException exception) {
            return exception.getStatusCode().value();
        }
    }

    private Long seedAdmin(String username, String dni) {
        PersonaEntity persona = personas.saveAndFlush(new PersonaEntity(null, "Admin", username, dni,
                username + "@test.local", "900000001"));
        return usuarios.saveAndFlush(new UsuarioEntity(null, username, "hash-" + username, EstadoUsuario.ACTIVO,
                LocalDateTime.now(), RolEnum.ADMIN, persona)).getUsuarioId();
    }
}
