CREATE DATABASE IF NOT EXISTS carpinteria_circular;
USE carpinteria_circular;

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    correo VARCHAR(255) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    activo BIT(1) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    direccion VARCHAR(300),
    fecha_registro DATETIME(6) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    telefono VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    activo BIT(1) NOT NULL,
    cantidad INT NOT NULL,
    categoria VARCHAR(100),
    descripcion VARCHAR(500),
    imagen VARCHAR(255),
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS pedidos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    estado ENUM(
        'CANCELADO',
        'COMPLETADO',
        'EN_PROCESO',
        'PENDIENTE'
    ) NOT NULL,
    fecha DATETIME(6) NOT NULL,
    observaciones VARCHAR(500),
    total DECIMAL(12,2) NOT NULL,
    cliente_id BIGINT NOT NULL,

    CONSTRAINT fk_pedidos_clientes
        FOREIGN KEY (cliente_id)
        REFERENCES clientes(id)
);

CREATE TABLE IF NOT EXISTS detalle_pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    pedido_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,

    CONSTRAINT fk_detalle_pedido_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES pedidos(id),

    CONSTRAINT fk_detalle_pedido_producto
        FOREIGN KEY (producto_id)
        REFERENCES productos(id)
);

CREATE TABLE IF NOT EXISTS envios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    direccion_entrega VARCHAR(300) NOT NULL,
    estado ENUM(
        'CANCELADO',
        'ENTREGADO',
        'EN_CAMINO',
        'PENDIENTE',
        'PREPARANDO'
    ) NOT NULL,
    fecha_entrega DATETIME(6),
    fecha_envio DATETIME(6),
    fecha_registro DATETIME(6) NOT NULL,
    numero_seguimiento VARCHAR(100),
    observaciones VARCHAR(500),
    transportista VARCHAR(120),
    pedido_id BIGINT NOT NULL UNIQUE,

    CONSTRAINT fk_envios_pedidos
        FOREIGN KEY (pedido_id)
        REFERENCES pedidos(id)
);