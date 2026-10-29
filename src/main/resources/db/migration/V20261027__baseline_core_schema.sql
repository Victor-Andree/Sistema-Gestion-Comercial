CREATE TABLE personas (
    persona_id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    dni VARCHAR(255) NOT NULL,
    correo VARCHAR(255) NOT NULL,
    telefono VARCHAR(255) NOT NULL,
    PRIMARY KEY (persona_id)
) ENGINE=InnoDB;

CREATE TABLE usuarios (
    usuario_id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    estado_usuario VARCHAR(20) NULL,
    fecha_registro DATETIME(6) NULL,
    rol VARCHAR(20) NULL,
    persona_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id),
    CONSTRAINT uk_usuarios_username UNIQUE (username),
    CONSTRAINT uk_usuarios_password UNIQUE (password),
    CONSTRAINT uk_usuarios_persona UNIQUE (persona_id),
    CONSTRAINT fk_usuarios_persona FOREIGN KEY (persona_id) REFERENCES personas (persona_id)
) ENGINE=InnoDB;

CREATE TABLE clientes (
    cliente_id BIGINT NOT NULL AUTO_INCREMENT,
    persona_id BIGINT NOT NULL,
    fecha_registro DATETIME(6) NOT NULL,
    PRIMARY KEY (cliente_id),
    CONSTRAINT uk_clientes_persona UNIQUE (persona_id),
    CONSTRAINT fk_clientes_persona FOREIGN KEY (persona_id) REFERENCES personas (persona_id)
) ENGINE=InnoDB;

CREATE TABLE tipo_producto (
    tipo_producto_id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    PRIMARY KEY (tipo_producto_id),
    CONSTRAINT uk_tipo_producto_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE productos (
    producto_id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    precio_venta DOUBLE NOT NULL,
    stock_minimo INT NOT NULL,
    estado_producto VARCHAR(20) NULL,
    tipo_producto_id BIGINT NOT NULL,
    PRIMARY KEY (producto_id),
    CONSTRAINT uk_productos_nombre UNIQUE (nombre),
    CONSTRAINT fk_productos_tipo FOREIGN KEY (tipo_producto_id) REFERENCES tipo_producto (tipo_producto_id)
) ENGINE=InnoDB;

CREATE TABLE stocks (
    stock_id BIGINT NOT NULL AUTO_INCREMENT,
    producto_id BIGINT NOT NULL,
    cantidad_actual INT NOT NULL,
    fecha_actualizacion DATETIME(6) NOT NULL,
    PRIMARY KEY (stock_id),
    CONSTRAINT uk_stocks_producto UNIQUE (producto_id),
    CONSTRAINT fk_stocks_producto FOREIGN KEY (producto_id) REFERENCES productos (producto_id)
) ENGINE=InnoDB;

CREATE TABLE ventas (
    venta_id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    fecha_venta DATETIME(6) NOT NULL,
    total DOUBLE NOT NULL,
    estado VARCHAR(20) NULL,
    PRIMARY KEY (venta_id),
    CONSTRAINT fk_ventas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (usuario_id),
    CONSTRAINT fk_ventas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (cliente_id)
) ENGINE=InnoDB;

CREATE TABLE detalle_ventas (
    detalle_venta_id BIGINT NOT NULL AUTO_INCREMENT,
    venta_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DOUBLE NOT NULL,
    subtotal DOUBLE NOT NULL,
    PRIMARY KEY (detalle_venta_id),
    CONSTRAINT fk_detalle_ventas_venta FOREIGN KEY (venta_id) REFERENCES ventas (venta_id),
    CONSTRAINT fk_detalle_ventas_producto FOREIGN KEY (producto_id) REFERENCES productos (producto_id)
) ENGINE=InnoDB;

CREATE TABLE comprobantes (
    comprobante_id BIGINT NOT NULL AUTO_INCREMENT,
    venta_id BIGINT NOT NULL,
    tipo VARCHAR(20) NULL,
    correlativo VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NULL,
    fecha_emision DATETIME(6) NOT NULL,
    PRIMARY KEY (comprobante_id),
    CONSTRAINT uk_comprobantes_venta UNIQUE (venta_id),
    CONSTRAINT fk_comprobantes_venta FOREIGN KEY (venta_id) REFERENCES ventas (venta_id)
) ENGINE=InnoDB;
