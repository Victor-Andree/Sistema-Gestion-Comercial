package com.gestion.sgc.infraestructure.repository;

import com.gestion.sgc.infraestructure.entity.UsuarioEntity;
import com.gestion.sgc.domain.aggregates.constans.EstadoUsuario;
import com.gestion.sgc.domain.aggregates.constans.RolEnum;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByUsernameAndUsuarioIdNot(String username, Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UsuarioEntity u where u.rol = :rol and u.estadoUsuario = :estado order by u.usuarioId")
    List<UsuarioEntity> lockByRolAndEstado(@Param("rol") RolEnum rol,
                                           @Param("estado") EstadoUsuario estado);

}
