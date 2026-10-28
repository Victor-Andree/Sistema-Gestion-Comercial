ALTER TABLE movimientos_caja
    ADD COLUMN idempotency_key VARCHAR(64) NULL AFTER movimiento_caja_id;

UPDATE movimientos_caja
SET idempotency_key = CONCAT('legacy-', movimiento_caja_id)
WHERE idempotency_key IS NULL;

ALTER TABLE movimientos_caja
    MODIFY idempotency_key VARCHAR(64) NOT NULL,
    ADD CONSTRAINT uk_movimiento_caja_idempotency UNIQUE (idempotency_key);
