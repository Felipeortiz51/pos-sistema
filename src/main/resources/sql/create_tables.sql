-- Script de creación de tablas para el Sistema POS
-- Base de datos: SQLite

-- Habilitar soporte de claves foráneas (importante en SQLite)
PRAGMA foreign_keys = ON;

-- =====================================================
-- TABLA: productos
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
-- TABLA: ventas
-- Almacena la información general de cada transacción.
-- =====================================================
CREATE TABLE IF NOT EXISTS ventas (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha_venta  TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total        REAL NOT NULL
);

-- =====================================================
-- TABLA: detalle_ventas
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
-- TABLA: usuarios
-- Almacena usuarios, contraseñas hasheadas y roles.
-- Roles: 'admin', 'cajero'
-- =====================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_usuario  TEXT NOT NULL UNIQUE,
    hash_contrasena TEXT NOT NULL,
    rol             TEXT NOT NULL CHECK(rol IN ('admin', 'cajero'))
);

-- Insertar un usuario administrador por defecto para el primer inicio
-- Contraseña: admin
INSERT OR IGNORE INTO usuarios (nombre_usuario, hash_contrasena, rol) VALUES ('admin', '$2a$10$lc61.NxFiCiO0yEySWhHzuEPSUHOyERFOV8jMzFyDnWD9Bi8V7Qa6', 'admin');