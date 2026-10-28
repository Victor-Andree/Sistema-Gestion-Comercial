CREATE TABLE movimientos_caja (
    movimiento_caja_id BIGINT NOT NULL AUTO_INCREMENT,
    sesion_caja_id BIGINT NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    concepto VARCHAR(200) NOT NULL,
    fecha_movimiento DATETIME(6) NOT NULL,
    usuario_responsable_id BIGINT NOT NULL,
    PRIMARY KEY (movimiento_caja_id),
    CONSTRAINT ck_movimiento_caja_tipo CHECK (tipo IN ('INGRESO', 'EGRESO')),
    CONSTRAINT ck_movimiento_caja_monto CHECK (monto > 0),
    CONSTRAINT fk_movimiento_caja_sesion FOREIGN KEY (sesion_caja_id)
        REFERENCES sesiones_caja (sesion_caja_id),
    CONSTRAINT fk_movimiento_caja_usuario FOREIGN KEY (usuario_responsable_id)
        REFERENCES usuarios (usuario_id),
    KEY idx_movimiento_caja_sesion_fecha (sesion_caja_id, fecha_movimiento),
    KEY idx_movimiento_caja_usuario (usuario_responsable_id)
) ENGINE=InnoDB;
