CREATE TABLE devoluciones_caja_pendientes (
    devolucion_caja_id BIGINT NOT NULL AUTO_INCREMENT,
    venta_id BIGINT NOT NULL,
    caja_id BIGINT NOT NULL,
    movimiento_cobro_id BIGINT NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'PENDIENTE',
    fecha_registro DATETIME(6) NOT NULL,
    movimiento_compensacion_id BIGINT NULL,
    PRIMARY KEY (devolucion_caja_id),
    CONSTRAINT uk_devolucion_caja_venta UNIQUE (venta_id),
    CONSTRAINT ck_devolucion_caja_monto CHECK (monto > 0),
    CONSTRAINT ck_devolucion_caja_estado CHECK (estado IN ('PENDIENTE', 'APLICADA')),
    CONSTRAINT fk_devolucion_caja_venta FOREIGN KEY (venta_id) REFERENCES ventas (venta_id),
    CONSTRAINT fk_devolucion_caja_caja FOREIGN KEY (caja_id) REFERENCES cajas (caja_id),
    CONSTRAINT fk_devolucion_caja_cobro FOREIGN KEY (movimiento_cobro_id) REFERENCES movimientos_caja (movimiento_caja_id),
    CONSTRAINT fk_devolucion_caja_compensacion FOREIGN KEY (movimiento_compensacion_id) REFERENCES movimientos_caja (movimiento_caja_id),
    INDEX idx_devolucion_caja_estado_caja (estado, caja_id)
) ENGINE=InnoDB;
