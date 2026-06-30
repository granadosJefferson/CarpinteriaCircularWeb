
USE carpinteria_circular;

-- Creates initial users for administrative access and client testing.
INSERT INTO usuarios (correo, nombre, password, rol)
VALUES
('admin@carpinteria.com', 'Administrador', '1234', 'ADMIN'),
('cliente@correo.com', 'Cliente de prueba', '1234', 'CLIENTE');

-- Loads sample clients used by the initial orders.
INSERT INTO clientes (
    activo,
    correo,
    direccion,
    fecha_registro,
    nombre,
    telefono
)
VALUES
(
    1,
    'maria@correo.com',
    'San José, Costa Rica',
    NOW(),
    'María Rodríguez',
    '8888-1111'
),
(
    1,
    'carlos@correo.com',
    'Heredia, Costa Rica',
    NOW(),
    'Carlos Vargas',
    '8777-2222'
);

-- Loads the initial catalog and inventory quantities.
INSERT INTO productos (
    activo,
    cantidad,
    categoria,
    descripcion,
    imagen,
    nombre,
    precio
)
VALUES
(
    1,
    10,
    'Mesas',
    'Mesa artesanal fabricada con madera recuperada.',
    NULL,
    'Mesa artesanal',
    95000.00
),
(
    1,
    15,
    'Decoración',
    'Repisa de madera con acabado natural.',
    NULL,
    'Repisa de madera',
    28000.00
),
(
    1,
    20,
    'Cocina',
    'Tabla artesanal para cocina.',
    NULL,
    'Tabla de cocina',
    15000.00
);

-- Each order references an existing client through cliente_id.
INSERT INTO pedidos (
    estado,
    fecha,
    observaciones,
    total,
    cliente_id
)
VALUES
(
    'PENDIENTE',
    NOW(),
    'Pedido de prueba.',
    123000.00,
    1
),
(
    'COMPLETADO',
    NOW(),
    'Pedido entregado correctamente.',
    30000.00,
    2
);

-- Stores the products, quantities and historical unit prices for each order.
INSERT INTO detalle_pedido (
    cantidad,
    precio_unitario,
    pedido_id,
    producto_id
)
VALUES
(1, 95000.00, 1, 1),
(1, 28000.00, 1, 2),
(2, 15000.00, 2, 3);

-- Creates the shipment associated with the first sample order.
INSERT INTO envios (
    direccion_entrega,
    estado,
    fecha_entrega,
    fecha_envio,
    fecha_registro,
    numero_seguimiento,
    observaciones,
    transportista,
    pedido_id
)
VALUES
(
    'San José, Costa Rica',
    'PENDIENTE',
    NULL,
    NULL,
    NOW(),
    'ENV-001',
    'Envío pendiente de preparación.',
    'Transporte local',
    1
);

