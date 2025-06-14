package com.tuempresa.pos.dao;

import com.tuempresa.pos.config.DatabaseManager;
import com.tuempresa.pos.model.DetalleVenta;
import com.tuempresa.pos.model.Venta;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

/**
 * Data Access Object (DAO) para la entidad Venta.
 * La conexión se obtiene una vez y se mantiene abierta durante las operaciones.
 */
public class VentaDAO {

    public double sumarVentasHoy() {
        String sql = "SELECT SUM(total) FROM ventas WHERE DATE(fecha_venta) = DATE('now', 'localtime')";
        Connection conn = DatabaseManager.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al sumar ventas de hoy: " + e.getMessage());
        }
        return 0.0;
    }

    public boolean registrarVenta(Venta venta) {
        Connection conn = DatabaseManager.getConnection();
        String sqlVenta = "INSERT INTO ventas (total, fecha_venta) VALUES (?, ?)";
        String sqlDetalle = "INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";

        try {
            // Iniciar transacción
            conn.setAutoCommit(false);

            // 1. Insertar la venta
            try (PreparedStatement pstmtVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                pstmtVenta.setDouble(1, venta.getTotal());
                pstmtVenta.setString(2, venta.getFechaVenta().toString());
                pstmtVenta.executeUpdate();

                try (ResultSet generatedKeys = pstmtVenta.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int ventaId = generatedKeys.getInt(1);

                        // 2. Insertar cada detalle
                        try (PreparedStatement pstmtDetalle = conn.prepareStatement(sqlDetalle)) {
                            ProductoDAO productoDAO = new ProductoDAO();
                            for (DetalleVenta detalle : venta.getDetalles()) {
                                pstmtDetalle.setInt(1, ventaId);
                                pstmtDetalle.setInt(2, detalle.getProducto().getId());
                                pstmtDetalle.setInt(3, detalle.getCantidad());
                                pstmtDetalle.setDouble(4, detalle.getPrecioUnitario());
                                pstmtDetalle.addBatch();

                                // 3. Actualizar stock usando la misma conexión transaccional
                                productoDAO.actualizarStock(conn, detalle.getProducto().getId(), detalle.getCantidad());
                            }
                            pstmtDetalle.executeBatch();
                        }
                    } else {
                        throw new SQLException("Error al crear la venta, no se obtuvo ID.");
                    }
                }
            }
            conn.commit(); // Confirmar transacción si todo fue bien
            return true;

        } catch (SQLException e) {
            System.err.println("Error transaccional al registrar la venta. Realizando rollback: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Deshacer cambios
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaurar modo auto-commit
                } catch (SQLException e) {
                    System.err.println("Error al restaurar auto-commit: " + e.getMessage());
                }
            }
        }
    }
    // En VentaDAO.java

    public double sumarVentasAyer() {
        // Usamos las funciones de fecha de SQLite para obtener el día anterior
        String sql = "SELECT SUM(total) FROM ventas WHERE DATE(fecha_venta) = DATE('now', '-1 day', 'localtime')";
        Connection conn = DatabaseManager.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al sumar ventas de ayer: " + e.getMessage());
        }
        return 0.0;
    }
    public List<Venta> getVentasPorRangoDeFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Venta> ventas = new ArrayList<>();
        // El formato de fecha en la BD es 'YYYY-MM-DD HH:MM:SS.sss'
        // Usamos la función DATE() de SQLite para comparar solo la parte de la fecha.
        String sql = "SELECT * FROM ventas WHERE DATE(fecha_venta) BETWEEN ? AND ? ORDER BY fecha_venta DESC";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fechaInicio.toString());
            pstmt.setString(2, fechaFin.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Venta venta = new Venta();
                    venta.setId(rs.getInt("id"));
                    venta.setTotal(rs.getDouble("total"));
                    // SQLite no tiene un tipo de dato de fecha nativo, lo leemos como String y lo parseamos
                    venta.setFechaVenta(java.time.LocalDateTime.parse(rs.getString("fecha_venta")));

                    // Por cada venta, cargamos sus detalles
                    venta.setDetalles(getDetallesPorVentaId(venta.getId()));

                    ventas.add(venta);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas por rango de fechas: " + e.getMessage());
        }
        return ventas;
    }

    private List<DetalleVenta> getDetallesPorVentaId(int idVenta) {
        List<DetalleVenta> detalles = new ArrayList<>();
        String sql = "SELECT * FROM detalle_ventas WHERE id_venta = ?";
        Connection conn = DatabaseManager.getConnection();
        ProductoDAO productoDAO = new ProductoDAO(); // Para buscar el producto de cada detalle

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idVenta);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta detalle = new DetalleVenta();
                    detalle.setId(rs.getInt("id"));
                    detalle.setIdVenta(rs.getInt("id_venta"));
                    detalle.setCantidad(rs.getInt("cantidad"));
                    detalle.setPrecioUnitario(rs.getDouble("precio_unitario"));

                    // Buscamos el objeto Producto completo usando el id_producto
                    int idProducto = rs.getInt("id_producto");
                    detalle.setProducto(productoDAO.buscarPorId(idProducto));

                    detalles.add(detalle);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener detalles de venta: " + e.getMessage());
        }
        return detalles;
    }

    public Map<LocalDate, Double> getVentasDeUltimos7Dias() {
        Map<LocalDate, Double> ventasPorDia = new HashMap<>();
        // Esta consulta agrupa las ventas por día y suma los totales de los últimos 7 días.
        String sql = "SELECT DATE(fecha_venta) as dia, SUM(total) as total_dia " +
                "FROM ventas " +
                "WHERE DATE(fecha_venta) >= DATE('now', '-6 days', 'localtime') " +
                "GROUP BY dia " +
                "ORDER BY dia ASC";

        Connection conn = DatabaseManager.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Convertimos el texto de la fecha de la BD a un objeto LocalDate de Java
                LocalDate fecha = LocalDate.parse(rs.getString("dia"));
                double total = rs.getDouble("total_dia");
                ventasPorDia.put(fecha, total);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas de los últimos 7 días: " + e.getMessage());
        }
        return ventasPorDia;
    }
}