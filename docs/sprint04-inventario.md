# Sprint 04 · Productos e inventario

## Despliegue

Antes de habilitar el formulario, ejecutar `db/migrations/sprint04_product_constraints.sql` en la base de datos MySQL de la aplicación. Retira los índices únicos erróneos de descripción, precio y mínimo; conserva la unicidad de nombre. Hibernate `ddl-auto=update` puede conservar los índices antiguos. El script no se ejecuta automáticamente al iniciar la aplicación y es reutilizable.

## Contratos

`POST /api/v1/productos` admite el campo opcional `cantidadInicial` (entero >= 0). Si se envía, producto y stock se guardan en la misma transacción: un fallo revierte ambos registros. Si se omite, se mantiene el contrato anterior que permite inicializar stock con `POST /api/v1/stocks/initial`. Para productos creados con cantidadInicial no debe volver a llamarse a ese endpoint.

`PUT /api/v1/productos/{id}` actualiza los datos del catálogo; no cambia las existencias. El frontend omite cantidadInicial durante la edición. Estado admite ACTIVO o INACTIVO. Los movimientos exigen cantidades > 0 y los ajustes permiten cero, pero nunca valores negativos. Se rechazan también ingresos que desbordan un Integer.

Productos, stock y categorías requieren ADMIN o ASISTENTE. Crear/eliminar categorías y ajustar el inventario físico requieren ADMIN. La seguridad de métodos está activa y las anotaciones usan roles compatibles con las autoridades ROLE_ del sistema. Esto también hace efectivas las restricciones ya declaradas en ventas y clientes, incluida la anulación de ventas para ADMIN.

`/api/v1/productos/stock-bajo` utiliza las existencias reales comparadas con el mínimo por producto. CORS permite PATCH desde localhost:3000, además de los métodos existentes.

Errores de inventario: 400 para datos inválidos con `message` y, cuando corresponde, `errors` por campo; 404 para referencias inexistentes; 409 para duplicados, stock insuficiente o integridad relacional. No se exponen detalles internos de SQL.

## Validación

`./mvnw.cmd test` ejecuta pruebas unitarias, HTTP/seguridad y persistencia H2 sin conectarse a MySQL. H2 está limitado al alcance de pruebas. Se prueba la reversión de creación ante fallo de stock, precios/mínimos repetidos y preservación de existencias al editar. H2 no valida el script específico de migración MySQL.

Antes del despliegue: aplicar la migración en una copia de la base real y comprobar crear/editar, duplicados, inactivos y permisos con usuarios ADMIN/ASISTENTE. Las pruebas locales no sustituyen esa comprobación.
