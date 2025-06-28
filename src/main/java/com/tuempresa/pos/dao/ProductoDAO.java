package com.tuempresa.pos.dao;

import com.tuempresa.pos.config.DatabaseManager;
import com.tuempresa.pos.model.Producto;
import com.tuempresa.pos.service.SessionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    /**
     * Obtiene todos los productos de un local específico.
     * @param idLocal El ID del local del que se quieren obtener los productos.
     * @return Una lista de productos.
     */
    public List<Producto> getAllProductos(int idLocal) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM productos WHERE id_local = ? ORDER BY nombre ASC";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idLocal);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    productos.add(crearProductoDesdeResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los productos por local: " + e.getMessage());
            e.printStackTrace();
        }
        return productos;
    }

    /**
     * Crea un nuevo producto asociado a un local.
     * @param producto El objeto producto con los datos a insertar.
     * @param idLocal El ID del local al que pertenece el producto.
     * @return true si la creación fue exitosa.
     */
    public boolean crearProducto(Producto producto, int idLocal) {
        String sql = "INSERT INTO productos(id_local, nombre, codigo_barras, precio, stock, descripcion) VALUES(?,?,?,?,?,?)";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idLocal);
            pstmt.setString(2, producto.getNombre());
            pstmt.setString(3, producto.getCodigoBarras());
            pstmt.setBigDecimal(4, java.math.BigDecimal.valueOf(producto.getPrecio())); // Usar BigDecimal para NUMERIC
            pstmt.setInt(5, producto.getStock());
            pstmt.setString(6, producto.getDescripcion());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza la información de un producto existente.
     * No necesita el idLocal porque el ID del producto es único.
     * @param producto El objeto producto con los datos actualizados.
     * @return true si la actualización fue exitosa.
     */
    public boolean actualizarProducto(Producto producto) {
        String sql = "UPDATE productos SET nombre = ?, codigo_barras = ?, precio = ?, stock = ?, descripcion = ? WHERE id = ?";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, producto.getNombre());
            pstmt.setString(2, producto.getCodigoBarras());
            pstmt.setBigDecimal(3, java.math.BigDecimal.valueOf(producto.getPrecio()));
            pstmt.setInt(4, producto.getStock());
            pstmt.setString(5, producto.getDescripcion());
            pstmt.setInt(6, producto.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un producto de la base de datos por su ID.
     * @param idProducto El ID del producto a eliminar.
     * @return true si la eliminación fue exitosa.
     */
    public boolean eliminarProducto(int idProducto) {
        String sql = "DELETE FROM productos WHERE id = ?";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idProducto);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca un producto por su código de barras dentro de un local específico.
     * @param codigoBarras El código de barras a buscar.
     * @param idLocal El ID del local donde buscar.
     * @return El objeto Producto si se encuentra, o null.
     */
    public Producto buscarPorCodigo(String codigoBarras, int idLocal) {
        String sql = "SELECT * FROM productos WHERE codigo_barras = ? AND id_local = ?";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigoBarras);
            pstmt.setInt(2, idLocal);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return crearProductoDesdeResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar producto por código: " + e.getMessage());
        }
        return null;
    }

    /**
     * Actualiza el stock de un producto como parte de una transacción.
     * @param conn La conexión transaccional existente.
     * @param idProducto El ID del producto.
     * @param cantidadVendida La cantidad a restar del stock.
     * @return true si la actualización fue exitosa.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean actualizarStock(Connection conn, int idProducto, int cantidadVendida) throws SQLException {
        String sql = "UPDATE productos SET stock = stock - ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cantidadVendida);
            pstmt.setInt(2, idProducto);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Busca un producto por su ID único global.
     * @param id El ID del producto.
     * @return El objeto Producto si se encuentra, o null.
     */
    public Producto buscarPorId(int id) {
        String sql = "SELECT * FROM productos WHERE id = ?";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return crearProductoDesdeResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar producto por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Método de ayuda para crear un objeto Producto desde un ResultSet de una consulta.
     * @param rs El ResultSet de la consulta.
     * @return Un objeto Producto.
     * @throws SQLException Si ocurre un error al leer del ResultSet.
     */
    private Producto crearProductoDesdeResultSet(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId(rs.getInt("id"));
        producto.setNombre(rs.getString("nombre"));
        producto.setCodigoBarras(rs.getString("codigo_barras"));
        // Para tipos NUMERIC/DECIMAL en la base de datos, es mejor usar BigDecimal en Java.
        // Lo convertimos a double al final para mantener la compatibilidad con el modelo actual.
        producto.setPrecio(rs.getBigDecimal("precio").doubleValue());
        producto.setStock(rs.getInt("stock"));
        producto.setDescripcion(rs.getString("descripcion"));
        return producto;
    }
    public int contarProductosActivos(int idLocal) {
        String sql = "SELECT COUNT(*) FROM productos WHERE stock > 0 AND id_local = ?";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idLocal);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al contar productos activos: " + e.getMessage());
        }
        return 0;
    }

    public int contarAlertasStock(int umbral, int idLocal) {
        String sql = "SELECT COUNT(*) FROM productos WHERE stock > 0 AND stock <= ? AND id_local = ?";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, umbral);
            pstmt.setInt(2, idLocal);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al contar alertas de stock: " + e.getMessage());
        }
        return 0;
    }
}