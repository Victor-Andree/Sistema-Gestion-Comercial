package com.gestion.sgc;

import com.gestion.sgc.application.useCase.CajaUseCase;
import com.gestion.sgc.application.useCase.SaldoCajaCalculator;
import com.gestion.sgc.domain.aggregates.constans.EstadoUsuario;
import com.gestion.sgc.domain.aggregates.constans.RolEnum;
import com.gestion.sgc.domain.ports.outputs.SesionCajaRepositoryPort;
import com.gestion.sgc.infraestructure.adapters.CajaRepositoryAdapter;
import com.gestion.sgc.infraestructure.adapters.SesionCajaRepositoryAdapter;
import com.gestion.sgc.infraestructure.adapters.auth.UsuarioRepositoryAdapter;
import com.gestion.sgc.infraestructure.entity.CajaEntity;
import com.gestion.sgc.infraestructure.entity.PersonaEntity;
import com.gestion.sgc.infraestructure.entity.UsuarioEntity;
import com.gestion.sgc.infraestructure.mapper.CajaMapperImpl;
import com.gestion.sgc.infraestructure.mapper.SesionCajaMapperImpl;
import com.gestion.sgc.infraestructure.mapper.UsuarioMapperImpl;
import com.gestion.sgc.infraestructure.repository.CajaJpaRepository;
import com.gestion.sgc.infraestructure.repository.PersonaJpaRepository;
import com.gestion.sgc.infraestructure.repository.SesionCajaJpaRepository;
import com.gestion.sgc.infraestructure.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@DataJpaTest(bootstrapMode = org.springframework.data.repository.config.BootstrapMode.LAZY, properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "logging.level.org.hibernate.SQL=WARN"
})
@Import({CajaUseCase.class, CajaRepositoryAdapter.class, SesionCajaRepositoryAdapter.class,
        UsuarioRepositoryAdapter.class, CajaMapperImpl.class, SesionCajaMapperImpl.class, UsuarioMapperImpl.class})
class CajaPersistenceTests {
    @Autowired CajaUseCase useCase;
    @Autowired CajaJpaRepository cajas;
    @Autowired SesionCajaJpaRepository sesiones;
    @Autowired PersonaJpaRepository personas;
    @Autowired UsuarioJpaRepository usuarios;
    @MockitoSpyBean SesionCajaRepositoryPort sesionPort;
    @MockitoBean SaldoCajaCalculator saldoCalculator;

    @AfterEach
    void cleanDatabase() {
        sesiones.deleteAll();
        cajas.deleteAll();
        usuarios.deleteAll();
        personas.deleteAll();
    }

    private Long seed() {
        PersonaEntity persona = new PersonaEntity(null, "Admin", "Caja", "70000001",
                "caja@nexo.test", "900000001");
        persona = personas.saveAndFlush(persona);
        usuarios.saveAndFlush(new UsuarioEntity(null, "admin-caja", "hash", EstadoUsuario.ACTIVO,
                LocalDateTime.now(), RolEnum.ADMIN, persona));
        return cajas.saveAndFlush(new CajaEntity(null, "Caja concurrente", true)).getCajaId();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void simultaneousOpeningsPersistOnlyOneSession() throws Exception {
        Long cajaId = seed();
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> attempt = () -> {
            start.await();
            try {
                useCase.abrir(cajaId, new BigDecimal("10.00"), "admin-caja");
                return 201;
            } catch (ResponseStatusException exception) {
                return exception.getStatusCode().value();
            }
        };

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(attempt);
            var second = executor.submit(attempt);
            start.countDown();
            List<Integer> statuses = List.of(first.get(), second.get());
            assertTrue(statuses.contains(201));
            assertTrue(statuses.contains(409));
        }
        assertEquals(1, sesiones.count());
        assertEquals(0, useCase.obtenerSesionActual(cajaId).montoInicial().compareTo(new BigDecimal("10.00")));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void persistenceFailureRollsBackOpening() {
        Long cajaId = seed();
        doThrow(new IllegalStateException("Fallo simulado")).when(sesionPort).save(any());
        assertThrows(IllegalStateException.class,
                () -> useCase.abrir(cajaId, new BigDecimal("20.00"), "admin-caja"));
        assertEquals(0, sesiones.count());
    }
}
