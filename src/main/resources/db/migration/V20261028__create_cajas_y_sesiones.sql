CREATE TABLE cajas (
    caja_id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (caja_id),
    UNIQUE KEY uk_cajas_nombre (nombre)
) ENGINE=InnoDB;

CREATE TABLE sesiones_caja (
    sesion_caja_id BIGINT NOT NULL AUTO_INCREMENT,
    caja_id BIGINT NOT NULL,
    estado VARCHAR(10) NOT NULL,
    monto_inicial DECIMAL(15,2) NOT NULL,
    fecha_apertura DATETIME(6) NOT NULL,
    usuario_apertura_id BIGINT NOT NULL,
    caja_abierta_id BIGINT GENERATED ALWAYS AS (
        CASE WHEN estado = 'ABIERTA' THEN caja_id ELSE NULL END
    ) STORED,
    PRIMARY KEY (sesion_caja_id),
    CONSTRAINT ck_sesion_caja_estado CHECK (estado IN ('ABIERTA', 'CERRADA')),
    CONSTRAINT ck_sesion_caja_monto_inicial CHECK (monto_inicial >= 0),
    CONSTRAINT fk_sesion_caja_caja FOREIGN KEY (caja_id) REFERENCES cajas (caja_id),
    CONSTRAINT fk_sesion_caja_usuario FOREIGN KEY (usuario_apertura_id) REFERENCES usuarios (usuario_id),
    UNIQUE KEY uk_sesion_abierta_por_caja (caja_abierta_id),
    KEY idx_sesion_caja_estado (caja_id, estado)
) ENGINE=InnoDB;

INSERT INTO cajas (nombre, activa)
SELECT 'Caja principal', TRUE
WHERE NOT EXISTS (SELECT 1 FROM cajas WHERE nombre = 'Caja principal');
