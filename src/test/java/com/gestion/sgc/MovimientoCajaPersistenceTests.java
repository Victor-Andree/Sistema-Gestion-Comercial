package com.gestion.sgc;

import com.gestion.sgc.application.dto.request.MovimientoCajaRequest;
import com.gestion.sgc.application.useCase.MovimientoCajaUseCase;
import com.gestion.sgc.domain.aggregates.constans.EstadoSesionCaja;
import com.gestion.sgc.domain.aggregates.constans.EstadoUsuario;
import com.gestion.sgc.domain.aggregates.constans.RolEnum;
import com.gestion.sgc.domain.aggregates.constans.TipoMovimientoCaja;
import com.gestion.sgc.domain.ports.outputs.MovimientoCajaRepositoryPort;
import com.gestion.sgc.infraestructure.adapters.MovimientoCajaRepositoryAdapter;
import com.gestion.sgc.infraestructure.adapters.SesionCajaRepositoryAdapter;
import com.gestion.sgc.infraestructure.adapters.auth.UsuarioRepositoryAdapter;
import com.gestion.sgc.infraestructure.entity.CajaEntity;
import com.gestion.sgc.infraestructure.entity.PersonaEntity;
import com.gestion.sgc.infraestructure.entity.SesionCajaEntity;
import com.gestion.sgc.infraestructure.entity.UsuarioEntity;
import com.gestion.sgc.infraestructure.mapper.CajaMapperImpl;
import com.gestion.sgc.infraestructure.mapper.MovimientoCajaMapperImpl;
import com.gestion.sgc.infraestructure.mapper.SesionCajaMapperImpl;
import com.gestion.sgc.infraestructure.mapper.UsuarioMapperImpl;
import com.gestion.sgc.infraestructure.repository.CajaJpaRepository;
import com.gestion.sgc.infraestructure.repository.MovimientoCajaJpaRepository;
import com.gestion.sgc.infraestructure.repository.PersonaJpaRepository;
import com.gestion.sgc.infraestructure.repository.SesionCajaJpaRepository;
import com.gestion.sgc.infraestructure.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
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
import java.util.UUID;

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
@Import({MovimientoCajaUseCase.class, MovimientoCajaRepositoryAdapter.class, SesionCajaRepositoryAdapter.class,
        UsuarioRepositoryAdapter.class, MovimientoCajaMapperImpl.class, SesionCajaMapperImpl.class,
        CajaMapperImpl.class, UsuarioMapperImpl.class})
class MovimientoCajaPersistenceTests {
    @Autowired MovimientoCajaUseCase useCase;
    @Autowired MovimientoCajaJpaRepository movimientos;
    @Autowired SesionCajaJpaRepository sesiones;
    @Autowired CajaJpaRepository cajas;
    @Autowired UsuarioJpaRepository usuarios;
    @Autowired PersonaJpaRepository personas;
    @MockitoSpyBean MovimientoCajaRepositoryPort movementPort;

    @AfterEach
    void cleanDatabase() {
        movimientos.deleteAll(); sesiones.deleteAll(); cajas.deleteAll(); usuarios.deleteAll(); personas.deleteAll();
    }

    private Long seed(String initialAmount) {
        PersonaEntity persona = personas.saveAndFlush(new PersonaEntity(null, "Admin", "Caja", "70000003",
                "movimientos@nexo.test", "900000003"));
        UsuarioEntity user = usuarios.saveAndFlush(new UsuarioEntity(null, "admin-movimientos", "hash",
                EstadoUsuario.ACTIVO, LocalDateTime.now(), RolEnum.ADMIN, persona));
        CajaEntity caja = cajas.saveAndFlush(new CajaEntity(null, "Caja movimientos", true));
        return sesiones.saveAndFlush(new SesionCajaEntity(null, caja, EstadoSesionCaja.ABIERTA,
                new BigDecimal(initialAmount), LocalDateTime.now(), user)).getSesionCajaId();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void simultaneousExpensesThatExceedBalancePersistOnlyOne() throws Exception {
        Long sessionId = seed("100.00");
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> attempt = () -> {
            start.await();
            try {
                useCase.registrar(sessionId, new MovimientoCajaRequest(TipoMovimientoCaja.EGRESO,
                        new BigDecimal("70.00"), "Egreso concurrente"), "admin-movimientos",
                        UUID.randomUUID().toString());
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
        assertEquals(1, movimientos.count());
        assertEquals(new BigDecimal("30.00"), useCase.resumen(sessionId).saldoProvisional());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void persistenceFailureRollsBackMovement() {
        Long sessionId = seed("100.00");
        doThrow(new IllegalStateException("Fallo simulado")).when(movementPort).save(any());
        assertThrows(IllegalStateException.class, () -> useCase.registrar(sessionId,
                new MovimientoCajaRequest(TipoMovimientoCaja.INGRESO, new BigDecimal("10.00"), "Ingreso"),
                "admin-movimientos", UUID.randomUUID().toString()));
        assertEquals(0, movimientos.count());
    }
}
