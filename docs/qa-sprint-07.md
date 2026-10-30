# QA final Sprint 07 — Administración de usuarios

Fecha de ejecución: 2026-10-30.
Ramas revisadas: `feature/administracion-usuarios` en backend y frontend.

## Resultado

- El último administrador activo no puede ser desactivado, bloqueado ni degradado a ASISTENTE.
- Las modificaciones concurrentes de administradores activos se serializan mediante bloqueo pesimista.
- El registro de usuarios requiere rol ADMIN; una solicitud anónima recibe 401 y ASISTENTE recibe 403.
- La administración bajo `/api/v1/usuarios` requiere ADMIN.
- Los JWT de usuarios INACTIVOS o BLOQUEADOS no crean un contexto autenticado y reciben 401.
- Creación, edición, contraseña opcional en edición, roles y cambios de estado conservan sus validaciones.

## Evidencia automatizada

- Backend: `84` pruebas, `0` fallos, `0` errores, `0` omitidas.
- Concurrencia: dos desactivaciones simultáneas sobre dos ADMIN activos producen una aceptación y un `409`; persiste exactamente un ADMIN activo.
- Frontend: `pnpm exec tsc --noEmit` aprobado.
- Regresión de productos: `5` pruebas aprobadas.
- Producción: `pnpm build` aprobado; 14 rutas generadas.

## Cobertura funcional

| Riesgo | Evidencia |
| --- | --- |
| Último ADMIN | `UsuarioAdministrationPersistenceTests` |
| Registro público y roles | `AuthRegistrationSecurityTests` y `UsuarioControllerTests` |
| JWT de cuentas sin acceso | `UserDetailsServiceImplTests` y `UsuarioControllerTests` |
| Caja, inventario, reportes y ventas | suite completa Maven |
| Productos y contratos frontend | `scripts/products.test.cjs`, TypeScript y build |

El proyecto no tiene configurado JaCoCo ni una herramienta equivalente, por lo que no se declara un porcentaje de cobertura de líneas. La cobertura informada es funcional y verificable mediante los casos anteriores.

## Observaciones no bloqueantes

- Maven conserva advertencias existentes de MapStruct sobre propiedades no mapeadas.
- Spring Security emite una advertencia informativa por el `AuthenticationProvider` configurado explícitamente.
- No se realizó merge a `main`.
