ALTER TABLE sesiones_caja
    ADD COLUMN saldo_esperado DECIMAL(15,2) NULL,
    ADD COLUMN monto_contado DECIMAL(15,2) NULL,
    ADD COLUMN diferencia DECIMAL(15,2) NULL,
    ADD COLUMN fecha_cierre DATETIME(6) NULL,
    ADD COLUMN usuario_cierre_id BIGINT NULL,
    ADD COLUMN observacion_cierre VARCHAR(500) NULL,
    ADD CONSTRAINT fk_sesion_caja_usuario_cierre
        FOREIGN KEY (usuario_cierre_id) REFERENCES usuarios (usuario_id),
    ADD CONSTRAINT ck_sesion_caja_monto_contado
        CHECK (monto_contado IS NULL OR monto_contado >= 0),
    ADD KEY idx_sesion_caja_fecha_cierre (fecha_cierre),
    ADD KEY idx_sesion_caja_usuario_cierre (usuario_cierre_id);

-- Compatibilidad con sesiones cerradas antes de US-04. Se conserva un arqueo técnico
-- neutro y auditable, sin inventar una diferencia histórica.
UPDATE sesiones_caja s
LEFT JOIN (
    SELECT sesion_caja_id,
           COALESCE(SUM(CASE WHEN tipo = 'INGRESO' THEN monto ELSE 0 END), 0) AS ingresos,
           COALESCE(SUM(CASE WHEN tipo = 'EGRESO' THEN monto ELSE 0 END), 0) AS egresos
    FROM movimientos_caja
    GROUP BY sesion_caja_id
) m ON m.sesion_caja_id = s.sesion_caja_id
SET s.saldo_esperado = s.monto_inicial + COALESCE(m.ingresos, 0) - COALESCE(m.egresos, 0),
    s.monto_contado = s.monto_inicial + COALESCE(m.ingresos, 0) - COALESCE(m.egresos, 0),
    s.diferencia = 0.00,
    s.fecha_cierre = s.fecha_apertura,
    s.usuario_cierre_id = s.usuario_apertura_id,
    s.observacion_cierre = 'Cierre previo a US-04 migrado sin diferencia histórica'
WHERE s.estado = 'CERRADA';

ALTER TABLE sesiones_caja
    ADD CONSTRAINT ck_sesion_caja_cierre_completo CHECK (
        (estado = 'ABIERTA' AND saldo_esperado IS NULL AND monto_contado IS NULL
            AND diferencia IS NULL AND fecha_cierre IS NULL AND usuario_cierre_id IS NULL)
        OR
        (estado = 'CERRADA' AND saldo_esperado IS NOT NULL AND monto_contado IS NOT NULL
            AND diferencia IS NOT NULL AND fecha_cierre IS NOT NULL AND usuario_cierre_id IS NOT NULL)
    ),
    ADD CONSTRAINT ck_sesion_caja_diferencia
        CHECK (diferencia IS NULL OR diferencia = monto_contado - saldo_esperado);
