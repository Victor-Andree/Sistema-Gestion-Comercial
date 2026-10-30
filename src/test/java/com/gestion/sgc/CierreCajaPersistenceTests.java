package com.gestion.sgc;

import com.gestion.sgc.application.dto.request.CierreCajaRequest;
import com.gestion.sgc.application.useCase.CajaUseCase;
import com.gestion.sgc.application.useCase.SaldoCajaCalculator;
import com.gestion.sgc.domain.aggregates.constans.EstadoSesionCaja;
import com.gestion.sgc.domain.aggregates.constans.EstadoUsuario;
import com.gestion.sgc.domain.aggregates.constans.RolEnum;
import com.gestion.sgc.domain.ports.outputs.SesionCajaRepositoryPort;
import com.gestion.sgc.infraestructure.adapters.CajaRepositoryAdapter;
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
@Import({CajaUseCase.class, SaldoCajaCalculator.class, CajaRepositoryAdapter.class,
        SesionCajaRepositoryAdapter.class, MovimientoCajaRepositoryAdapter.class, UsuarioRepositoryAdapter.class,
        CajaMapperImpl.class, SesionCajaMapperImpl.class, MovimientoCajaMapperImpl.class, UsuarioMapperImpl.class})
class CierreCajaPersistenceTests {
    @Autowired CajaUseCase useCase;
    @Autowired SesionCajaJpaRepository sesiones;
    @Autowired MovimientoCajaJpaRepository movimientos;
    @Autowired CajaJpaRepository cajas;
    @Autowired UsuarioJpaRepository usuarios;
    @Autowired PersonaJpaRepository personas;
    @MockitoSpyBean SesionCajaRepositoryPort sesionPort;

    @AfterEach
    void cleanDatabase() {
        movimientos.deleteAll(); sesiones.deleteAll(); cajas.deleteAll(); usuarios.deleteAll(); personas.deleteAll();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void simultaneousClosingsPersistOnlyOneClosure() throws Exception {
        Long sessionId = seed("100.00");
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> attempt = () -> {
            start.await();
            try {
                useCase.cerrar(sessionId, new CierreCajaRequest(new BigDecimal("100.00"), "Cierre concurrente"),
                        "admin-cierre");
                return 200;
            } catch (ResponseStatusException exception) {
                return exception.getStatusCode().value();
            }
        };

        List<Integer> statuses;
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(attempt);
            var second = executor.submit(attempt);
            start.countDown();
            statuses = List.of(first.get(), second.get());
        }

        assertTrue(statuses.contains(200));
        assertTrue(statuses.contains(409));
        SesionCajaEntity closed = sesiones.findById(sessionId).orElseThrow();
        assertEquals(EstadoSesionCaja.CERRADA, closed.getEstado());
        assertEquals(new BigDecimal("100.00"), closed.getSaldoEsperado());
        assertEquals(BigDecimal.ZERO.setScale(2), closed.getDiferencia());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void persistenceFailureRollsBackClosure() {
        Long sessionId = seed("80.00");
        doThrow(new IllegalStateException("Fallo simulado")).when(sesionPort).save(any());

        assertThrows(IllegalStateException.class, () -> useCase.cerrar(sessionId,
                new CierreCajaRequest(new BigDecimal("80.00"), null), "admin-cierre"));

        SesionCajaEntity open = sesiones.findById(sessionId).orElseThrow();
        assertEquals(EstadoSesionCaja.ABIERTA, open.getEstado());
        assertEquals(null, open.getFechaCierre());
    }

    private Long seed(String initialAmount) {
        PersonaEntity persona = personas.saveAndFlush(new PersonaEntity(null, "Admin", "Cierre", "70000009",
                "cierre@registraditos.test", "900000009"));
        UsuarioEntity user = usuarios.saveAndFlush(new UsuarioEntity(null, "admin-cierre", "hash-cierre",
                EstadoUsuario.ACTIVO, LocalDateTime.now(), RolEnum.ADMIN, persona));
        CajaEntity caja = cajas.saveAndFlush(new CajaEntity(null, "Caja cierre", true));
        return sesiones.saveAndFlush(new SesionCajaEntity(null, caja, EstadoSesionCaja.ABIERTA,
                new BigDecimal(initialAmount), LocalDateTime.now(), user)).getSesionCajaId();
    }
}
