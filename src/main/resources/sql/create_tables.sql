-- Script de creación de tablas para el Sistema POS
-- Base de datos: SQLite
-- ACTUALIZACIÓN: Agregar soporte para múltiples formas de pago

-- Habilitar soporte de claves foráneas (importante en SQLite)
PRAGMA foreign_keys = ON;

-- =====================================================
-- TABLA: productos (sin cambios)
-- Almacena la información de cada producto disponible.
-- =====================================================
CREATE TABLE IF NOT EXISTS productos (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre        TEXT NOT NULL,
    codigo_barras TEXT NOT NULL UNIQUE,
    precio        REAL NOT NULL,
    stock         INTEGER NOT NULL DEFAULT 0,
    descripcion   TEXT
);

-- =====================================================
-- TABLA: ventas (sin cambios)
-- Almacena la información general de cada transacción.
-- =====================================================
CREATE TABLE IF NOT EXISTS ventas (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha_venta  TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total        REAL NOT NULL
);

-- =====================================================
-- TABLA: detalle_ventas (sin cambios)
-- Tabla de enlace que detalla qué productos se vendieron
-- en cada venta, la cantidad y el precio en ese momento.
-- =====================================================
CREATE TABLE IF NOT EXISTS detalle_ventas (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    id_venta       INTEGER NOT NULL,
    id_producto    INTEGER NOT NULL,
    cantidad       INTEGER NOT NULL,
    precio_unitario REAL NOT NULL,
    FOREIGN KEY (id_venta) REFERENCES ventas(id) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES productos(id)
);

-- =====================================================
-- TABLA: usuarios (sin cambios)
-- Almacena usuarios, contraseñas hasheadas y roles.
-- Roles: 'admin', 'cajero'
-- =====================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_usuario  TEXT NOT NULL UNIQUE,
    hash_contrasena TEXT NOT NULL,
    rol             TEXT NOT NULL CHECK(rol IN ('admin', 'cajero'))
);

-- =====================================================
-- NUEVA TABLA: detalle_pagos
-- Almacena los diferentes métodos de pago utilizados en cada venta
-- =====================================================
CREATE TABLE IF NOT EXISTS detalle_pagos (
    id                      INTEGER PRIMARY KEY AUTOINCREMENT,
    id_venta               INTEGER NOT NULL,
    tipo_pago              TEXT NOT NULL CHECK(tipo_pago IN ('EFECTIVO', 'TARJETA_CREDITO', 'TARJETA_DEBITO', 'TRANSFERENCIA', 'MIXTO')),
    monto                  REAL NOT NULL,
    referencia             TEXT,
    autorizacion           TEXT,
    fecha_pago             TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    banco                  TEXT,
    ultimos_cuatro_digitos TEXT,
    FOREIGN KEY (id_venta) REFERENCES ventas(id) ON DELETE CASCADE
);

-- =====================================================
-- NUEVA TABLA: configuracion_pagos
-- Configuración de métodos de pago y comisiones
-- =====================================================
CREATE TABLE IF NOT EXISTS configuracion_pagos (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo_pago         TEXT NOT NULL UNIQUE,
    activo            INTEGER NOT NULL DEFAULT 1,
    comision_porcentaje REAL DEFAULT 0.0,
    comision_fija     REAL DEFAULT 0.0,
    descripcion       TEXT,
    fecha_actualizacion TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- NUEVA TABLA: transacciones_fallidas
-- Log de transacciones que fallaron para auditoría
-- =====================================================
CREATE TABLE IF NOT EXISTS transacciones_fallidas (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    id_venta        INTEGER,
    tipo_pago       TEXT NOT NULL,
    monto           REAL NOT NULL,
    codigo_error    TEXT,
    mensaje_error   TEXT,
    fecha_intento   TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    datos_adicionales TEXT,
    FOREIGN KEY (id_venta) REFERENCES ventas(id)
);

-- =====================================================
-- ÍNDICES para optimización de consultas
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_detalle_pagos_venta ON detalle_pagos(id_venta);
CREATE INDEX IF NOT EXISTS idx_detalle_pagos_tipo ON detalle_pagos(tipo_pago);
CREATE INDEX IF NOT EXISTS idx_detalle_pagos_fecha ON detalle_pagos(fecha_pago);
CREATE INDEX IF NOT EXISTS idx_transacciones_fallidas_fecha ON transacciones_fallidas(fecha_intento);
CREATE INDEX IF NOT EXISTS idx_ventas_fecha ON ventas(fecha_venta);
CREATE INDEX IF NOT EXISTS idx_productos_codigo ON productos(codigo_barras);

-- =====================================================
-- DATOS INICIALES
-- =====================================================

-- Insertar un usuario administrador por defecto para el primer inicio
-- Contraseña: admin
INSERT OR IGNORE INTO usuarios (nombre_usuario, hash_contrasena, rol)
VALUES ('admin', '$2a$10$lc61.NxFiCiO0yEySWhHzuEPSUHOyERFOV8jMzFyDnWD9Bi8V7Qa6', 'admin');

-- Insertar configuración por defecto de métodos de pago
INSERT OR IGNORE INTO configuracion_pagos (tipo_pago, activo, comision_porcentaje, comision_fija, descripcion) VALUES
('EFECTIVO', 1, 0.0, 0.0, 'Pago en efectivo sin comisiones'),
('TARJETA_CREDITO', 1, 3.0, 0.0, 'Tarjeta de crédito con 3% de comisión'),
('TARJETA_DEBITO', 1, 1.5, 0.0, 'Tarjeta de débito con 1.5% de comisión'),
('TRANSFERENCIA', 1, 0.0, 2.0, 'Transferencia bancaria con comisión fija de $2.00'),
('MIXTO', 1, 0.0, 0.0, 'Combinación de múltiples métodos de pago');

-- =====================================================
-- VISTAS ÚTILES para reportes
-- =====================================================

-- Vista de ventas con desglose de pagos
CREATE VIEW IF NOT EXISTS vista_ventas_pagos AS
SELECT
    v.id as id_venta,
    v.fecha_venta,
    v.total as total_venta,
    GROUP_CONCAT(dp.tipo_pago || ': $' || dp.monto, ', ') as metodos_pago,
    COUNT(dp.id) as cantidad_pagos,
    SUM(dp.monto) as total_pagado
FROM ventas v
LEFT JOIN detalle_pagos dp ON v.id = dp.id_venta
GROUP BY v.id, v.fecha_venta, v.total;

-- Vista de comisiones por método de pago
CREATE VIEW IF NOT EXISTS vista_comisiones AS
SELECT
    dp.tipo_pago,
    COUNT(*) as transacciones,
    SUM(dp.monto) as monto_total,
    CASE
        WHEN dp.tipo_pago = 'TARJETA_CREDITO' THEN SUM(dp.monto * 0.03)
        WHEN dp.tipo_pago = 'TARJETA_DEBITO' THEN SUM(dp.monto * 0.015)
        WHEN dp.tipo_pago = 'TRANSFERENCIA' THEN COUNT(*) * 2.0
        ELSE 0
    END as comision_total
FROM detalle_pagos dp
GROUP BY dp.tipo_pago;

-- Vista de estadísticas diarias de pagos
CREATE VIEW IF NOT EXISTS vista_estadisticas_diarias AS
SELECT
    DATE(dp.fecha_pago) as fecha,
    dp.tipo_pago,
    COUNT(*) as transacciones,
    SUM(dp.monto) as total_monto,
    AVG(dp.monto) as promedio_monto
FROM detalle_pagos dp
GROUP BY DATE(dp.fecha_pago), dp.tipo_pago
ORDER BY fecha DESC, dp.tipo_pago;