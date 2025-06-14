package com.tuempresa.pos.dao;

import com.tuempresa.pos.config.DatabaseManager;
import com.tuempresa.pos.model.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        String sql = "SELECT * FROM usuarios WHERE nombre_usuario = ?";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombreUsuario);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getInt("id"));
                    usuario.setNombreUsuario(rs.getString("nombre_usuario"));
                    usuario.setHashContrasena(rs.getString("hash_contrasena"));
                    usuario.setRol(rs.getString("rol"));
                    return usuario;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
        }
        return null;
    }

    public boolean verificarContrasena(String contrasenaPlana, String hashAlmacenado) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(contrasenaPlana, hashAlmacenado);
    }
}