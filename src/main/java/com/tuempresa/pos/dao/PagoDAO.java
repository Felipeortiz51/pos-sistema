package com.tuempresa.pos.dao;

import com.tuempresa.pos.config.DatabaseManager;
import com.tuempresa.pos.model.DetallePago;
import com.tuempresa.pos.model.TipoPago;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar los pagos en la base de datos
 */
public class PagoDAO {

    /**
     * Registra un pago en la base de datos
     */
    public boolean registrarPago(DetallePago pago) {
        String sql = "INSERT INTO detalle_pagos (id_venta, tipo_pago, monto, referencia, " +
                "autorizacion, fecha_pago, banco, ultimos_cuatro_digitos) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, pago.getIdVenta());
            pstmt.setString(2, pago.getTipo().name());
            pstmt.setDouble(3, pago.getMonto());
            pstmt.setString(4, pago.getReferencia());
            pstmt.setString(5, pago.getAutorizacion());
            pstmt.setString(6, pago.getFechaPago().toString());
            pstmt.setString(7, pago.getBanco());
            pstmt.setString(8, pago.getUltimosCuatroDigitos());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        pago.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al registrar pago: " + e.getMessage());
        }
        return false;
    }

    /**
     * Registra múltiples pagos para una venta
     */
    public boolean registrarPagos(List<DetallePago> pagos, int idVenta) {
        Connection conn = DatabaseManager.getConnection();
        try {
            conn.setAutoCommit(false);

            for (DetallePago pago : pagos) {
                pago.setIdVenta(idVenta);
                if (!registrarPago(pago)) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al registrar pagos múltiples: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error al restaurar autocommit: " + e.getMessage());
            }
        }
    }

    /**
     * Obtiene todos los pagos de una venta
     */
    public List<DetallePago> getPagosPorVenta(int idVenta) {
        List<DetallePago> pagos = new ArrayList<>();
        String sql = "SELECT * FROM detalle_pagos WHERE id_venta = ? ORDER BY fecha_pago";

        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idVenta);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DetallePago pago = crearDetallePagoDesdeResultSet(rs);
                    pagos.add(pago);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener pagos por venta: " + e.getMessage());
        }

        return pagos;
    }

    /**
     * Obtiene estadísticas de pagos por tipo
     */
    public java.util.Map<TipoPago, Double> getEstadisticasPorTipoPago(java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) {
        java.util.Map<TipoPago, Double> estadisticas = new java.util.HashMap<>();

        String sql = "SELECT tipo_pago, SUM(monto) as total " +
                "FROM detalle_pagos dp " +
                "JOIN ventas v ON dp.id_venta = v.id " +
                "WHERE DATE(v.fecha_venta) BETWEEN ? AND ? " +
                "GROUP BY tipo_pago";

        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fechaInicio.toString());
            pstmt.setString(2, fechaFin.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TipoPago tipo = TipoPago.valueOf(rs.getString("tipo_pago"));
                    double total = rs.getDouble("total");
                    estadisticas.put(tipo, total);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas de pagos: " + e.getMessage());
        }

        return estadisticas;
    }

    /**
     * Cuenta las transacciones por tipo de pago en un período
     */
    public java.util.Map<TipoPago, Integer> getContadorTransaccionesPorTipo(java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) {
        java.util.Map<TipoPago, Integer> contadores = new java.util.HashMap<>();

        String sql = "SELECT tipo_pago, COUNT(*) as cantidad " +
                "FROM detalle_pagos dp " +
                "JOIN ventas v ON dp.id_venta = v.id " +
                "WHERE DATE(v.fecha_venta) BETWEEN ? AND ? " +
                "GROUP BY tipo_pago";

        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fechaInicio.toString());
            pstmt.setString(2, fechaFin.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TipoPago tipo = TipoPago.valueOf(rs.getString("tipo_pago"));
                    int cantidad = rs.getInt("cantidad");
                    contadores.put(tipo, cantidad);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener contadores de transacciones: " + e.getMessage());
        }

        return contadores;
    }

    /**
     * Obtiene el total de comisiones por período
     */
    public double getTotalComisiones(java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) {
        String sql = "SELECT SUM(CASE " +
                "    WHEN tipo_pago = 'TARJETA_CREDITO' THEN monto * 0.03 " +
                "    WHEN tipo_pago = 'TARJETA_DEBITO' THEN monto * 0.015 " +
                "    WHEN tipo_pago = 'TRANSFERENCIA' THEN 2.0 " +
                "    ELSE 0 " +
                "END) as total_comisiones " +
                "FROM detalle_pagos dp " +
                "JOIN ventas v ON dp.id_venta = v.id " +
                "WHERE DATE(v.fecha_venta) BETWEEN ? AND ?";

        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fechaInicio.toString());
            pstmt.setString(2, fechaFin.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_comisiones");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular comisiones: " + e.getMessage());
        }

        return 0.0;
    }

    /**
     * Busca pagos por referencia
     */
    public List<DetallePago> buscarPorReferencia(String referencia) {
        List<DetallePago> pagos = new ArrayList<>();
        String sql = "SELECT * FROM detalle_pagos WHERE referencia LIKE ? ORDER BY fecha_pago DESC";

        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + referencia + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DetallePago pago = crearDetallePagoDesdeResultSet(rs);
                    pagos.add(pago);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar pagos por referencia: " + e.getMessage());
        }

        return pagos;
    }

    /**
     * Elimina un pago (para cancelaciones)
     */
    public boolean eliminarPago(int idPago) {
        String sql = "DELETE FROM detalle_pagos WHERE id = ?";

        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idPago);
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar pago: " + e.getMessage());
            return false;
        }
    }

    /**
     * Crea un objeto DetallePago desde un ResultSet
     */
    private DetallePago crearDetallePagoDesdeResultSet(ResultSet rs) throws SQLException {
        DetallePago pago = new DetallePago();
        pago.setId(rs.getInt("id"));
        pago.setIdVenta(rs.getInt("id_venta"));
        pago.setTipo(TipoPago.valueOf(rs.getString("tipo_pago")));
        pago.setMonto(rs.getDouble("monto"));
        pago.setReferencia(rs.getString("referencia"));
        pago.setAutorizacion(rs.getString("autorizacion"));
        pago.setFechaPago(java.time.LocalDateTime.parse(rs.getString("fecha_pago")));
        pago.setBanco(rs.getString("banco"));
        pago.setUltimosCuatroDigitos(rs.getString("ultimos_cuatro_digitos"));
        return pago;
    }
}