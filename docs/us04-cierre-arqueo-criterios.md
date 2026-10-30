# US-04 — Cierre y arqueo de caja

Documento de refinamiento. No forma parte de la implementación de Administración de usuarios.

## Criterios de aceptación propuestos

1. Solo una sesión `ABIERTA` puede cerrarse y el cierre lo realiza un usuario `ADMIN` autenticado.
2. El sistema calcula el saldo esperado con monto inicial, movimientos manuales, cobros en efectivo y devoluciones aplicadas.
3. El administrador registra el efectivo contado, una observación opcional y confirma el cierre.
4. Se persisten fecha, responsable, saldo esperado, monto contado y diferencia usando `DECIMAL(15,2)`/`BigDecimal`.
5. La operación bloquea la sesión y es transaccional e idempotente: dos solicitudes concurrentes producen un solo cierre.
6. Una sesión cerrada es inmutable y rechaza nuevos movimientos, ventas en efectivo y cierres repetidos con `409`.
7. Una diferencia distinta de cero queda visible y auditable; nunca se corrige eliminando movimientos.
8. `ADMIN` puede ejecutar y consultar el arqueo. `ASISTENTE` solo consulta el estado permitido por la política vigente.
9. Respuestas esperadas: `200/201`, `400`, `401`, `403`, `404` y `409`, con el formato de errores existente.
10. La interfaz incluye resumen, confirmación, estados loading/error/success y comportamiento responsive.

## QA mínimo

- Cierre exacto, con sobrante y con faltante.
- Sesión inexistente, ya cerrada y sin JWT.
- Permisos ADMIN/ASISTENTE.
- Dos cierres concurrentes sobre la misma sesión.
- Rollback completo ante error de persistencia.
- Regresión de aperturas, movimientos manuales y cobros de venta.
- Migración y concurrencia verificadas sobre MySQL 8.
