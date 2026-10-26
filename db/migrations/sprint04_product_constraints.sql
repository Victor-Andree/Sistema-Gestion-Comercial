-- Ejecutar en la base de datos de la aplicación después de revisar el resultado
-- del SELECT. No elimina datos: retira únicamente índices únicos de una sola
-- columna en descripcion, precio_venta y stock_minimo de productos.
-- La unicidad del nombre y los índices de claves foráneas se conservan.
-- Hibernate ddl-auto=update no garantiza retirar índices únicos existentes.
SET @product_table = (
    SELECT TABLE_NAME FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'productos'
    LIMIT 1
);
SET @drop_indexes = (
    SELECT GROUP_CONCAT(CONCAT('DROP INDEX `', REPLACE(idx.INDEX_NAME, '`', '``'), '`') SEPARATOR ', ')
    FROM (
        SELECT INDEX_NAME
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @product_table
          AND NON_UNIQUE = 0 AND INDEX_NAME <> 'PRIMARY'
        GROUP BY INDEX_NAME
        HAVING COUNT(*) = 1 AND MAX(COLUMN_NAME) IN ('descripcion', 'precio_venta', 'stock_minimo')
    ) idx
);
SET @migration_sql = IF(@drop_indexes IS NULL, 'SELECT ''No hay índices que retirar'' AS resultado',
    CONCAT('ALTER TABLE `', REPLACE(@product_table, '`', '``'), '` ', @drop_indexes));
SELECT @migration_sql AS cambio_a_ejecutar;
PREPARE product_constraints FROM @migration_sql;
EXECUTE product_constraints;
DEALLOCATE PREPARE product_constraints;
