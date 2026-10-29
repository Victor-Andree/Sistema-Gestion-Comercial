# Flyway: despliegue y baseline

Flyway se ejecuta automáticamente antes de que Hibernate valide el esquema. `ddl-auto` usa `validate` y
`clean` está deshabilitado. Ninguna migración contiene credenciales ni elimina datos.

## Base nueva

1. Crear una base MySQL 8 vacía.
2. Definir `DB_URL_LOCAL` o `DB_URL_QA`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET` fuera del repositorio.
3. Iniciar Spring Boot. Flyway aplicará `V20261027` y las migraciones posteriores.
4. Consultar `flyway_schema_history`; todas las filas deben tener `success = 1`.

Un segundo inicio valida checksums y no vuelve a ejecutar las migraciones aplicadas.

## Base existente sin historial Flyway

No activar globalmente `baseline-on-migrate`. Primero respaldar la base y auditar qué scripts fueron aplicados.

- Si la base contiene solo el esquema anterior a caja, establecer baseline `20261027` y después migrar.
- Si ya contiene manualmente todos los cambios hasta devoluciones pendientes, establecer baseline `20261101`.
- Si está en una versión intermedia, usar exactamente la última versión comprobada; nunca asumirla.

Para el único arranque de baseline, usar variables temporales:

```powershell
$env:SPRING_FLYWAY_BASELINE_ON_MIGRATE="true"
$env:SPRING_FLYWAY_BASELINE_VERSION="20261101" # sustituir por la versión auditada
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
Remove-Item Env:SPRING_FLYWAY_BASELINE_ON_MIGRATE
Remove-Item Env:SPRING_FLYWAY_BASELINE_VERSION
```

Después del primer inicio, confirmar `flyway_schema_history` y volver a ejecutar sin esas variables.

## Comprobaciones

```sql
SELECT installed_rank, version, description, type, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

No ejecutar `flyway clean` en bases con datos. Para nuevas modificaciones, crear una migración con una versión
superior; nunca editar un script que ya figure en `flyway_schema_history`.
