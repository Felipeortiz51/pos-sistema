package com.tuempresa.pos.dao;

import com.tuempresa.pos.config.DatabaseManager;
import com.tuempresa.pos.model.Local;
import com.tuempresa.pos.model.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {


    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        // Consulta SQL actualizada para unir usuarios y roles
        String sql = "SELECT u.*, r.nombre_rol FROM usuarios u " +
                "JOIN roles r ON u.id_rol = r.id " +
                "WHERE u.nombre_usuario = ?";

        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombreUsuario);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getInt("id"));
                    usuario.setNombreUsuario(rs.getString("nombre_usuario"));
                    usuario.setHashContrasena(rs.getString("hash_contrasena"));

                    // Leemos la columna "nombre_rol" que obtuvimos del JOIN
                    // y la asignamos al campo 'rol' del objeto Usuario.
                    // Esto mantiene la compatibilidad con el resto del código (ej. SessionManager).
                    usuario.setRol(rs.getString("nombre_rol"));

                    return usuario;
                }
            }
        } catch (SQLException e) {
            // Imprimimos el error de una forma más detallada para facilitar la depuración
            System.err.println("Error al buscar usuario: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    public List<Local> getLocalesPorUsuario(int idUsuario) {
        List<Local> locales = new ArrayList<>();
        String sql = "SELECT l.* FROM locales l " +
                "JOIN usuario_locales ul ON l.id = ul.id_local " +
                "WHERE ul.id_usuario = ?";

        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Local local = new Local();
                    local.setId(rs.getInt("id"));
                    local.setNombre(rs.getString("nombre_local"));
                    local.setDireccion(rs.getString("direccion"));
                    locales.add(local);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener locales por usuario: " + e.getMessage());
            e.printStackTrace();
        }
        return locales;
    }

    public boolean verificarContrasena(String contrasenaPlana, String hashAlmacenado) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(contrasenaPlana, hashAlmacenado);
    }
}