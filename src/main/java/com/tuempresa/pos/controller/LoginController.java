package com.tuempresa.pos.controller;

import com.tuempresa.pos.dao.UsuarioDAO;
import com.tuempresa.pos.model.Usuario;
import com.tuempresa.pos.service.SessionManager;
import com.tuempresa.pos.util.NotificationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;

    private UsuarioDAO usuarioDAO;

    @FXML
    public void initialize() {
        usuarioDAO = new UsuarioDAO();
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        System.out.println("--- Inicio de Intento de Login ---");
        System.out.println("Usuario introducido: '" + username + "'");
        System.out.println("Contraseña introducida: '" + password + "'");

        if (username.isEmpty() || password.isEmpty()) {
            NotificationUtil.mostrarAdvertencia("Campos vacíos", "Por favor, ingrese usuario y contraseña.");
            return;
        }

        Usuario usuario = usuarioDAO.buscarPorNombreUsuario(username);

        // Punto de control 1: ¿Se encontró el usuario en la BD?
        if (usuario == null) {
            System.out.println("DEBUG: El usuario '" + username + "' NO fue encontrado en la base de datos.");
        } else {
            System.out.println("DEBUG: Usuario '" + username + "' encontrado en la base de datos.");
            System.out.println("DEBUG: Hash almacenado: " + usuario.getHashContrasena());

            // Punto de control 2: ¿La contraseña coincide con el hash?
            boolean contrasenaValida = usuarioDAO.verificarContrasena(password, usuario.getHashContrasena());
            System.out.println("DEBUG: Resultado de la verificación de contraseña: " + contrasenaValida);

            if (contrasenaValida) {
                // Guardar sesión
                SessionManager.getInstance().setUsuarioActual(usuario);
                NotificationUtil.mostrarInformacion("Inicio de Sesión Exitoso", "Bienvenido, " + usuario.getNombreUsuario());

                // Abrir el dashboard
                abrirDashboard();

                // Cerrar la ventana de login
                Stage stage = (Stage) btnLogin.getScene().getWindow();
                stage.close();
                return; // Termina el método aquí para no mostrar el error de abajo
            }
        }

        // Si llegamos hasta aquí, es porque algo falló.
        System.out.println("--- Fin del Intento de Login (FALLIDO) ---");
        NotificationUtil.mostrarError("Error de Autenticación", "El usuario o la contraseña son incorrectos.");
    }

    private void abrirDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DashboardView.fxml"));
            Parent root = loader.load();
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Sistema de Punto de Venta - Panel Principal");
            dashboardStage.setScene(new Scene(root));

            // Línea clave: Maximiza la ventana al abrirla
            dashboardStage.setMaximized(true);

            dashboardStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error fatal al cargar el dashboard.");
        }
    }
}