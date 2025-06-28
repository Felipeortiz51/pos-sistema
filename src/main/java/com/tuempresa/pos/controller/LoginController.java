package com.tuempresa.pos.controller;

import com.tuempresa.pos.dao.UsuarioDAO;
import com.tuempresa.pos.model.Local;
import com.tuempresa.pos.model.Usuario;
import com.tuempresa.pos.service.SessionManager;
import com.tuempresa.pos.util.NotificationUtil;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private VBox loginFormContainer;
    @FXML private VBox brandingPanel;
    @FXML private HBox mainContainer;

    private UsuarioDAO usuarioDAO;

    @FXML
    public void initialize() {
        usuarioDAO = new UsuarioDAO();
        btnLogin.setDefaultButton(true);

        initializeEntryAnimations();
        setupButtonAnimations();
        setupInputAnimations();
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            animateError();
            NotificationUtil.mostrarAdvertencia("Campos vacíos", "Por favor, ingrese usuario y contraseña.");
            return;
        }

        Usuario usuario = usuarioDAO.buscarPorNombreUsuario(username);

        if (usuario != null && usuarioDAO.verificarContrasena(password, usuario.getHashContrasena())) {
            // --- NUEVA LÓGICA DE SELECCIÓN DE LOCAL ---
            List<Local> localesDisponibles = usuarioDAO.getLocalesPorUsuario(usuario.getId());

            if (localesDisponibles.isEmpty()) {
                animateError();
                NotificationUtil.mostrarError("Acceso Denegado", "Este usuario no tiene ningún local asignado. Contacte a un administrador.");
                return;
            }

            // Por ahora, seleccionamos automáticamente el primer local de la lista.
            // En el futuro, aquí se podría mostrar un diálogo de selección si hay más de uno.
            Local localSeleccionado = localesDisponibles.get(0);

            // Guardar usuario y local en la sesión global
            SessionManager.getInstance().setUsuarioActual(usuario);
            SessionManager.getInstance().setLocalActual(localSeleccionado);

            animateSuccess();
            NotificationUtil.mostrarInformacion("Inicio de Sesión Exitoso", "Bienvenido, " + usuario.getNombreUsuario() + " | Local: " + localSeleccionado.getNombre());

            animateExit(() -> {
                abrirDashboard();
                Stage stage = (Stage) btnLogin.getScene().getWindow();
                stage.close();
            });
            // --- FIN DE NUEVA LÓGICA ---

        } else {
            animateError();
            NotificationUtil.mostrarError("Error de Autenticación", "El usuario o la contraseña son incorrectos.");
        }
    }

    private void abrirDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DashboardView.fxml"));
            Parent root = loader.load();
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Sistema de Punto de Venta - Panel Principal");
            dashboardStage.setScene(new Scene(root));
            dashboardStage.setMaximized(true);
            dashboardStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error fatal al cargar el dashboard.");
        }
    }

    // --- MÉTODOS DE ANIMACIÓN (sin cambios) ---

    private void initializeEntryAnimations() {
        brandingPanel.setOpacity(0);
        brandingPanel.setTranslateX(-100);
        loginFormContainer.setOpacity(0);
        loginFormContainer.setTranslateX(100);

        FadeTransition fadeInBranding = new FadeTransition(Duration.millis(800), brandingPanel);
        fadeInBranding.setFromValue(0);
        fadeInBranding.setToValue(1);
        TranslateTransition slideBranding = new TranslateTransition(Duration.millis(800), brandingPanel);
        slideBranding.setFromX(-100);
        slideBranding.setToX(0);

        FadeTransition fadeInForm = new FadeTransition(Duration.millis(800), loginFormContainer);
        fadeInForm.setFromValue(0);
        fadeInForm.setToValue(1);
        fadeInForm.setDelay(Duration.millis(300));
        TranslateTransition slideForm = new TranslateTransition(Duration.millis(800), loginFormContainer);
        slideForm.setFromX(100);
        slideForm.setToX(0);
        slideForm.setDelay(Duration.millis(300));

        ParallelTransition entryAnimation = new ParallelTransition(fadeInBranding, slideBranding, fadeInForm, slideForm);
        entryAnimation.play();
    }

    private void setupButtonAnimations() {
        btnLogin.setOnMouseEntered(e -> animateButtonHover(true));
        btnLogin.setOnMouseExited(e -> animateButtonHover(false));
        btnLogin.setOnMousePressed(e -> animateButtonPress(true));
        btnLogin.setOnMouseReleased(e -> animateButtonPress(false));
    }

    private void animateButtonHover(boolean hover) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), btnLogin);
        if (hover) {
            scaleTransition.setToX(1.05);
            scaleTransition.setToY(1.05);
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web("#667eea", 0.4));
            shadow.setRadius(20);
            shadow.setOffsetY(8);
            btnLogin.setEffect(shadow);
        } else {
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web("#667eea", 0.3));
            shadow.setRadius(15);
            shadow.setOffsetY(5);
            btnLogin.setEffect(shadow);
        }
        scaleTransition.play();
    }

    private void animateButtonPress(boolean pressed) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), btnLogin);
        if (pressed) {
            scaleTransition.setToX(0.95);
            scaleTransition.setToY(0.95);
        } else {
            scaleTransition.setToX(1.05);
            scaleTransition.setToY(1.05);
        }
        scaleTransition.play();
    }

    private void setupInputAnimations() {
        txtUsername.focusedProperty().addListener((obs, oldVal, newVal) -> animateInputFocus(txtUsername, newVal));
        txtPassword.focusedProperty().addListener((obs, oldVal, newVal) -> animateInputFocus(txtPassword, newVal));
    }

    private void animateInputFocus(javafx.scene.control.Control input, boolean focused) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), input);
        if (focused) {
            scaleTransition.setToX(1.02);
            scaleTransition.setToY(1.02);
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#667eea", 0.3));
            glow.setRadius(10);
            input.setEffect(glow);
        } else {
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            input.setEffect(null);
        }
        scaleTransition.play();
    }

    private void animateError() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), loginFormContainer);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void animateSuccess() {
        ScaleTransition bounce = new ScaleTransition(Duration.millis(200), btnLogin);
        bounce.setFromX(1.0);
        bounce.setFromY(1.0);
        bounce.setToX(1.1);
        bounce.setToY(1.1);
        bounce.setCycleCount(2);
        bounce.setAutoReverse(true);
        bounce.play();
    }

    private void animateExit(Runnable onComplete) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), mainContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(500), mainContainer);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.8);
        scaleOut.setToY(0.8);
        ParallelTransition exitAnimation = new ParallelTransition(fadeOut, scaleOut);
        exitAnimation.setOnFinished(e -> onComplete.run());
        exitAnimation.play();
    }
}