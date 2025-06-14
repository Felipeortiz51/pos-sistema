package com.tuempresa.pos.controller;

import com.tuempresa.pos.dao.UsuarioDAO;
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
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import java.io.IOException;

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

        // Inicializar animaciones de entrada
        initializeEntryAnimations();

        // Configurar animaciones de hover en botones
        setupButtonAnimations();

        // Configurar animaciones de focus en inputs
        setupInputAnimations();
    }

    /**
     * Animaciones de entrada cuando se carga la pantalla
     */
    private void initializeEntryAnimations() {
        // Ocultar elementos inicialmente
        brandingPanel.setOpacity(0);
        brandingPanel.setTranslateX(-100);

        loginFormContainer.setOpacity(0);
        loginFormContainer.setTranslateX(100);

        // Animación del panel izquierdo (branding)
        FadeTransition fadeInBranding = new FadeTransition(Duration.millis(800), brandingPanel);
        fadeInBranding.setFromValue(0);
        fadeInBranding.setToValue(1);

        TranslateTransition slideBranding = new TranslateTransition(Duration.millis(800), brandingPanel);
        slideBranding.setFromX(-100);
        slideBranding.setToX(0);

        // Animación del formulario (con delay)
        FadeTransition fadeInForm = new FadeTransition(Duration.millis(800), loginFormContainer);
        fadeInForm.setFromValue(0);
        fadeInForm.setToValue(1);
        fadeInForm.setDelay(Duration.millis(300));

        TranslateTransition slideForm = new TranslateTransition(Duration.millis(800), loginFormContainer);
        slideForm.setFromX(100);
        slideForm.setToX(0);
        slideForm.setDelay(Duration.millis(300));

        // Ejecutar todas las animaciones
        ParallelTransition entryAnimation = new ParallelTransition(
                fadeInBranding, slideBranding, fadeInForm, slideForm
        );

        entryAnimation.play();
    }

    /**
     * Animaciones para el botón de login
     */
    private void setupButtonAnimations() {
        // Efecto de hover
        btnLogin.setOnMouseEntered(e -> animateButtonHover(true));
        btnLogin.setOnMouseExited(e -> animateButtonHover(false));

        // Efecto de click
        btnLogin.setOnMousePressed(e -> animateButtonPress(true));
        btnLogin.setOnMouseReleased(e -> animateButtonPress(false));
    }

    /**
     * Animación de hover para botones
     */
    private void animateButtonHover(boolean hover) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), btnLogin);

        if (hover) {
            scaleTransition.setToX(1.05);
            scaleTransition.setToY(1.05);

            // Agregar sombra más intensa
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web("#667eea", 0.4));
            shadow.setRadius(20);
            shadow.setOffsetY(8);
            btnLogin.setEffect(shadow);

        } else {
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);

            // Sombra normal
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web("#667eea", 0.3));
            shadow.setRadius(15);
            shadow.setOffsetY(5);
            btnLogin.setEffect(shadow);
        }

        scaleTransition.play();
    }

    /**
     * Animación de presión para botones
     */
    private void animateButtonPress(boolean pressed) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), btnLogin);

        if (pressed) {
            scaleTransition.setToX(0.95);
            scaleTransition.setToY(0.95);
        } else {
            scaleTransition.setToX(1.05); // Volver al estado hover
            scaleTransition.setToY(1.05);
        }

        scaleTransition.play();
    }

    /**
     * Animaciones para los campos de entrada
     */
    private void setupInputAnimations() {
        // Username field
        txtUsername.focusedProperty().addListener((obs, oldVal, newVal) -> {
            animateInputFocus(txtUsername, newVal);
        });

        // Password field
        txtPassword.focusedProperty().addListener((obs, oldVal, newVal) -> {
            animateInputFocus(txtPassword, newVal);
        });
    }

    /**
     * Animación de focus para inputs
     */
    private void animateInputFocus(javafx.scene.control.Control input, boolean focused) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), input);

        if (focused) {
            scaleTransition.setToX(1.02);
            scaleTransition.setToY(1.02);

            // Agregar glow effect
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

    /**
     * Animación de error (shake)
     */
    private void animateError() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), loginFormContainer);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);

        // Cambiar color del borde temporalmente
        String originalStyle = loginFormContainer.getStyle();
        loginFormContainer.setStyle(originalStyle + "; -fx-border-color: #e53e3e; -fx-border-width: 2px;");

        shake.setOnFinished(e -> {
            loginFormContainer.setStyle(originalStyle);
        });

        shake.play();
    }

    /**
     * Animación de éxito (bounce)
     */
    private void animateSuccess() {
        ScaleTransition bounce = new ScaleTransition(Duration.millis(200), btnLogin);
        bounce.setFromX(1.0);
        bounce.setFromY(1.0);
        bounce.setToX(1.1);
        bounce.setToY(1.1);
        bounce.setCycleCount(2);
        bounce.setAutoReverse(true);

        // Cambiar temporalmente el color del botón
        String originalStyle = btnLogin.getStyle();
        btnLogin.setStyle(originalStyle.replace("#667eea, #764ba2", "#28a745, #20c997"));

        bounce.setOnFinished(e -> {
            btnLogin.setStyle(originalStyle);
        });

        bounce.play();
    }

    /**
     * Animación de loading en el botón (elegante, sin rotación)
     */
    private void animateLoading(boolean loading) {
        if (loading) {
            // Cambiar texto con animación suave
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), btnLogin);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.7);

            fadeOut.setOnFinished(e -> {
                btnLogin.setText("🔍 Verificando...");
                btnLogin.setDisable(true);

                // Efecto de pulso suave
                ScaleTransition pulse = new ScaleTransition(Duration.millis(800), btnLogin);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(1.02);
                pulse.setToY(1.02);
                pulse.setCycleCount(Timeline.INDEFINITE);
                pulse.setAutoReverse(true);
                pulse.play();

                // Parar después de 2 segundos
                Timeline stopLoading = new Timeline(new KeyFrame(Duration.seconds(2), ev -> {
                    pulse.stop();
                    btnLogin.setScaleX(1.0);
                    btnLogin.setScaleY(1.0);

                    // Restaurar texto
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), btnLogin);
                    fadeIn.setFromValue(0.7);
                    fadeIn.setToValue(1.0);
                    fadeIn.setOnFinished(ev2 -> {
                        btnLogin.setText("Acceder al Sistema");
                        btnLogin.setDisable(false);
                    });
                    fadeIn.play();
                }));
                stopLoading.play();
            });

            fadeOut.play();

        } else {
            btnLogin.setText("Acceder al Sistema");
            btnLogin.setDisable(false);
        }
    }

    /**
     * Animación de salida (cuando se cierra el login)
     */
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

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        System.out.println("--- Inicio de Intento de Login ---");
        System.out.println("Usuario introducido: '" + username + "'");
        System.out.println("Contraseña introducida: '" + password + "'");

        if (username.isEmpty() || password.isEmpty()) {
            animateError();
            NotificationUtil.mostrarAdvertencia("Campos vacíos", "Por favor, ingrese usuario y contraseña.");
            return;
        }

        // Mostrar animación de loading
        animateLoading(true);

        // Simular delay de verificación (en un caso real, esto sería asíncrono)
        Timeline delayedCheck = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
            animateLoading(false);

            Usuario usuario = usuarioDAO.buscarPorNombreUsuario(username);

            if (usuario == null) {
                System.out.println("DEBUG: El usuario '" + username + "' NO fue encontrado en la base de datos.");
                animateError();
            } else {
                System.out.println("DEBUG: Usuario '" + username + "' encontrado en la base de datos.");
                System.out.println("DEBUG: Hash almacenado: " + usuario.getHashContrasena());

                boolean contrasenaValida = usuarioDAO.verificarContrasena(password, usuario.getHashContrasena());
                System.out.println("DEBUG: Resultado de la verificación de contraseña: " + contrasenaValida);

                if (contrasenaValida) {
                    animateSuccess();

                    // Guardar sesión
                    SessionManager.getInstance().setUsuarioActual(usuario);
                    NotificationUtil.mostrarInformacion("Inicio de Sesión Exitoso", "Bienvenido, " + usuario.getNombreUsuario());

                    // Animación de salida antes de abrir dashboard
                    Timeline openDashboard = new Timeline(new KeyFrame(Duration.seconds(0.5), ev -> {
                        animateExit(() -> {
                            abrirDashboard();
                            Stage stage = (Stage) btnLogin.getScene().getWindow();
                            stage.close();
                        });
                    }));
                    openDashboard.play();

                    return;
                } else {
                    animateError();
                }
            }

            System.out.println("--- Fin del Intento de Login (FALLIDO) ---");
            NotificationUtil.mostrarError("Error de Autenticación", "El usuario o la contraseña son incorrectos.");
        }));

        delayedCheck.play();
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
}