ALTER TABLE ventas
    ADD COLUMN metodo_pago VARCHAR(20) NULL AFTER estado;

UPDATE ventas SET metodo_pago = 'NO_REGISTRADO' WHERE metodo_pago IS NULL;

ALTER TABLE ventas
    MODIFY metodo_pago VARCHAR(20) NOT NULL,
    ADD CONSTRAINT ck_venta_metodo_pago
        CHECK (metodo_pago IN ('EFECTIVO', 'YAPE', 'TRANSFERENCIA', 'NO_REGISTRADO'));

ALTER TABLE movimientos_caja
    ADD COLUMN origen VARCHAR(10) NOT NULL DEFAULT 'MANUAL' AFTER tipo,
    ADD COLUMN venta_id BIGINT NULL AFTER usuario_responsable_id,
    ADD COLUMN movimiento_original_id BIGINT NULL AFTER venta_id,
    ADD CONSTRAINT ck_movimiento_caja_origen CHECK (origen IN ('MANUAL', 'VENTA')),
    ADD CONSTRAINT fk_movimiento_caja_venta FOREIGN KEY (venta_id) REFERENCES ventas (venta_id),
    ADD CONSTRAINT fk_movimiento_caja_original FOREIGN KEY (movimiento_original_id)
        REFERENCES movimientos_caja (movimiento_caja_id),
    ADD UNIQUE KEY uk_movimiento_venta_tipo (venta_id, tipo),
    ADD KEY idx_movimiento_caja_venta (venta_id),
    ADD KEY idx_movimiento_caja_original (movimiento_original_id);
