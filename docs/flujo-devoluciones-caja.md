# Flujo de devoluciones de caja

Una venta anulada y una devolución de efectivo son hechos distintos. La venta puede quedar `ANULADA`
mientras la devolución de caja permanece `PENDIENTE`; en ese caso no debe comunicarse como dinero reembolsado.

## Estado actual

- Si la sesión del cobro sigue abierta, se registra un único `EGRESO` compensatorio.
- Si la sesión original está cerrada, se intenta registrar el egreso en la sesión abierta actual de la misma caja.
- Si no hay sesión abierta o su saldo es insuficiente, se crea una fila `PENDIENTE` en
  `devoluciones_caja_pendientes`. El movimiento histórico original nunca se modifica.
- Las pendientes se consultan mediante `GET /api/v1/cajas/devoluciones/pendientes`.

## Aplicación futura

La operación que marque una devolución como `APLICADA` deberá ejecutarse en una sola transacción:

1. bloquear la devolución pendiente y la sesión actual;
2. comprobar que continúa `PENDIENTE` y que existe saldo;
3. buscar primero `venta-{ventaId}-anulacion`;
4. crear el egreso solo si esa clave no existe;
5. guardar su identificador en `movimiento_compensacion_id` y cambiar el estado a `APLICADA`;
6. confirmar toda la transacción o revertirla completa.

La clave idempotente y la restricción única `(venta_id, tipo)` impiden egresos duplicados.
