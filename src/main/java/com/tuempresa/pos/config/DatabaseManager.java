package com.tuempresa.pos.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/pos_sistema_db";
    private static final String USER = "pos_user";
    private static final String PASSWORD = "d1e2f3t4"; // Usa la misma que creaste en el Paso 1
    private static Connection connection;

    private DatabaseManager() {}

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                System.out.println("Conexión a PostgreSQL establecida.");

            } catch (SQLException e) {
                System.err.println("Error al conectar a la base de datos: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return connection;
    }

    private static void inicializarBaseDeDatos() {
        // Carga el script desde el classpath de resources, forma más robusta
        try (InputStream is = DatabaseManager.class.getResourceAsStream("/sql/create_tables.sql");
             Statement stmt = connection.createStatement()) {

            if (is == null) {
                System.err.println("No se pudo encontrar el script create_tables.sql en resources.");
                return;
            }
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            stmt.executeUpdate(sql);
            System.out.println("Base de datos inicializada y tablas creadas (si no existían).");
        } catch (SQLException | IOException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Conexión a la base de datos cerrada.");
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}
