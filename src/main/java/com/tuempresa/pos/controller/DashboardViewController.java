package com.tuempresa.pos.controller;

import com.tuempresa.pos.service.SessionManager;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DashboardViewController {

    @FXML private VBox mainContentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnVentas;
    @FXML private Button btnProductos;
    @FXML private Button btnReportes;
    @FXML private Button btnAlertas;
    @FXML private Label lblHeaderTitle;
    @FXML private Label lblHeaderSubtitle;
    @FXML private Label lblFechaHora;
    @FXML private VBox sidebar;

    private List<Button> navButtons;
    private Button currentActiveButton;

    @FXML
    public void initialize() {
        navButtons = Arrays.asList(btnDashboard, btnProductos, btnVentas, btnReportes, btnAlertas);

        // Configurar animaciones
        setupSidebarAnimations();
        setupNavigationAnimations();

        // Inicializar
        actualizarFechaHora();
        aplicarPermisos();

        // Cargar dashboard con animación
        handleDashboardClick();
    }

    /**
     * Configurar animaciones del sidebar
     */
    private void setupSidebarAnimations() {
        // Animación de entrada del sidebar
        sidebar.setTranslateX(-280);
        sidebar.setOpacity(0);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(600), sidebar);
        slideIn.setFromX(-280);
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), sidebar);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition sidebarEntry = new ParallelTransition(slideIn, fadeIn);
        sidebarEntry.setDelay(Duration.millis(200));
        sidebarEntry.play();

        // Animación escalonada de los botones
        animateNavigationButtons();
    }

    /**
     * Animar botones de navegación uno por uno
     */
    private void animateNavigationButtons() {
        for (int i = 0; i < navButtons.size(); i++) {
            Button button = navButtons.get(i);
            if (button != null && button.isManaged()) {
                // Ocultar inicialmente
                button.setOpacity(0);
                button.setTranslateX(-50);

                // Animar con delay progresivo
                Duration delay = Duration.millis(400 + (i * 100));

                FadeTransition fade = new FadeTransition(Duration.millis(400), button);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.setDelay(delay);

                TranslateTransition slide = new TranslateTransition(Duration.millis(400), button);
                slide.setFromX(-50);
                slide.setToX(0);
                slide.setDelay(delay);
                slide.setInterpolator(Interpolator.EASE_OUT);

                ParallelTransition buttonAnim = new ParallelTransition(fade, slide);
                buttonAnim.play();
            }
        }
    }

    /**
     * Configurar efectos hover y click para navegación
     */
    private void setupNavigationAnimations() {
        for (Button button : navButtons) {
            if (button != null) {
                setupButtonHoverEffect(button);
                setupButtonClickEffect(button);
            }
        }
    }

    /**
     * Efecto hover para botones de navegación
     */
    private void setupButtonHoverEffect(Button button) {
        button.setOnMouseEntered(e -> {
            if (!button.getStyleClass().contains("nav-button-active")) {
                // Animación de hover
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), button);
                scale.setToX(1.02);
                scale.setToY(1.02);
                scale.play();

                // Agregar sombra sutil
                DropShadow shadow = new DropShadow();
                shadow.setColor(Color.web("#0d6efd", 0.2));
                shadow.setRadius(8);
                shadow.setOffsetY(2);
                button.setEffect(shadow);
            }
        });

        button.setOnMouseExited(e -> {
            if (!button.getStyleClass().contains("nav-button-active")) {
                // Volver al tamaño normal
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), button);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();

                button.setEffect(null);
            }
        });
    }

    /**
     * Efecto click para botones
     */
    private void setupButtonClickEffect(Button button) {
        button.setOnMousePressed(e -> {
            ScaleTransition press = new ScaleTransition(Duration.millis(100), button);
            press.setToX(0.95);
            press.setToY(0.95);
            press.play();
        });

        button.setOnMouseReleased(e -> {
            ScaleTransition release = new ScaleTransition(Duration.millis(100), button);
            release.setToX(1.02);
            release.setToY(1.02);
            release.play();
        });
    }

    /**
     * Animación de cambio de vista con transición suave
     */
    private void cambiarVistaConAnimacion(String fxmlFile, String titulo, String subtitulo) {
        // Fade out del contenido actual
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), mainContentArea);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // Slide hacia la izquierda
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), mainContentArea);
        slideOut.setFromX(0);
        slideOut.setToX(-50);

        ParallelTransition exitTransition = new ParallelTransition(fadeOut, slideOut);

        exitTransition.setOnFinished(e -> {
            // Cargar nueva vista
            cargarNuevaVista(fxmlFile);

            // Animar entrada de la nueva vista
            mainContentArea.setTranslateX(50);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), mainContentArea);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), mainContentArea);
            slideIn.setFromX(50);
            slideIn.setToX(0);
            slideIn.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition entryTransition = new ParallelTransition(fadeIn, slideIn);
            entryTransition.play();

            // Animar cambio de títulos
            animarCambioTitulos(titulo, subtitulo);
        });

        exitTransition.play();
    }

    /**
     * Cargar nueva vista sin animación
     */
    private void cargarNuevaVista(String fxmlFile) {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/" + fxmlFile);
            if (fxmlUrl == null) {
                System.err.println("ERROR: No se pudo encontrar el archivo FXML: " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();

            if (view instanceof javafx.scene.layout.Region) {
                ((javafx.scene.layout.Region) view).setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }

            if (fxmlFile.equals("DashboardContentView.fxml")) {
                DashboardContentController contentController = loader.getController();
                if (contentController != null) {
                    contentController.setNavegacionVentasAction(event -> handleVentasClick());
                }
            }

            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(view);

        } catch (IOException e) {
            System.err.println("Error de IO al cargar FXML: " + fxmlFile);
            e.printStackTrace();
        }
    }

    /**
     * Animar cambio de títulos del header
     */
    private void animarCambioTitulos(String nuevoTitulo, String nuevoSubtitulo) {
        // Fade out títulos actuales
        FadeTransition fadeOutTitle = new FadeTransition(Duration.millis(200), lblHeaderTitle);
        fadeOutTitle.setFromValue(1.0);
        fadeOutTitle.setToValue(0.0);

        FadeTransition fadeOutSubtitle = new FadeTransition(Duration.millis(200), lblHeaderSubtitle);
        fadeOutSubtitle.setFromValue(1.0);
        fadeOutSubtitle.setToValue(0.0);

        ParallelTransition fadeOutTitles = new ParallelTransition(fadeOutTitle, fadeOutSubtitle);

        fadeOutTitles.setOnFinished(e -> {
            // Cambiar texto
            lblHeaderTitle.setText(nuevoTitulo);
            lblHeaderSubtitle.setText(nuevoSubtitulo);

            // Fade in con nuevos títulos
            FadeTransition fadeInTitle = new FadeTransition(Duration.millis(300), lblHeaderTitle);
            fadeInTitle.setFromValue(0.0);
            fadeInTitle.setToValue(1.0);

            FadeTransition fadeInSubtitle = new FadeTransition(Duration.millis(300), lblHeaderSubtitle);
            fadeInSubtitle.setFromValue(0.0);
            fadeInSubtitle.setToValue(1.0);
            fadeInSubtitle.setDelay(Duration.millis(100));

            ParallelTransition fadeInTitles = new ParallelTransition(fadeInTitle, fadeInSubtitle);
            fadeInTitles.play();
        });

        fadeOutTitles.play();
    }

    /**
     * Animación del botón activo
     */
    private void setActiveButtonConAnimacion(Button activeButton) {
        // Remover clase activa de todos los botones
        for (Button button : navButtons) {
            if (button != null) {
                button.getStyleClass().removeAll("nav-button-active");

                // Animar vuelta al estado normal si no es el activo
                if (button != activeButton) {
                    ScaleTransition normalScale = new ScaleTransition(Duration.millis(200), button);
                    normalScale.setToX(1.0);
                    normalScale.setToY(1.0);
                    normalScale.play();
                    button.setEffect(null);
                }
            }
        }

        // Animar botón activo
        if (activeButton != null) {
            activeButton.getStyleClass().add("nav-button-active");

            // Efecto de "pulso" para indicar que está activo
            ScaleTransition activeScale = new ScaleTransition(Duration.millis(300), activeButton);
            activeScale.setFromX(1.0);
            activeScale.setFromY(1.0);
            activeScale.setToX(1.05);
            activeScale.setToY(1.05);
            activeScale.setCycleCount(2);
            activeScale.setAutoReverse(true);
            activeScale.setInterpolator(Interpolator.EASE_BOTH);

            // Sombra especial para botón activo
            DropShadow activeShadow = new DropShadow();
            activeShadow.setColor(Color.web("#0d6efd", 0.4));
            activeShadow.setRadius(12);
            activeShadow.setOffsetY(3);
            activeButton.setEffect(activeShadow);

            activeScale.play();
            currentActiveButton = activeButton;
        }
    }

    private void aplicarPermisos() {
        boolean isAdmin = SessionManager.getInstance().isAdmin();

        btnProductos.setManaged(isAdmin);
        btnProductos.setVisible(isAdmin);
        btnReportes.setManaged(isAdmin);
        btnReportes.setVisible(isAdmin);
        btnAlertas.setManaged(isAdmin);
        btnAlertas.setVisible(isAdmin);
    }

    private void actualizarFechaHora() {
        if (lblFechaHora != null) {
            LocalDateTime ahora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new Locale("es", "ES"));
            lblFechaHora.setText(ahora.format(formatter));

            // Animar la fecha con fade in
            lblFechaHora.setOpacity(0);
            FadeTransition fadeFecha = new FadeTransition(Duration.millis(800), lblFechaHora);
            fadeFecha.setFromValue(0);
            fadeFecha.setToValue(1);
            fadeFecha.setDelay(Duration.millis(1000));
            fadeFecha.play();
        }
    }

    // Métodos de navegación con animaciones
    @FXML
    void handleDashboardClick() {
        cambiarVistaConAnimacion("DashboardContentView.fxml", "Dashboard", "Resumen general del negocio");
        setActiveButtonConAnimacion(btnDashboard);
    }

    @FXML
    void handleVentasClick() {
        cambiarVistaConAnimacion("MainView.fxml", "Ventas", "Registrar nueva venta");
        setActiveButtonConAnimacion(btnVentas);
    }

    @FXML
    void handleProductosClick() {
        cambiarVistaConAnimacion("ProductManagementView.fxml", "Productos", "Gestión de inventario");
        setActiveButtonConAnimacion(btnProductos);
    }

    @FXML
    void handleReportesClick() {
        cambiarVistaConAnimacion("ReportsView.fxml", "Reportes", "Análisis y estadísticas de ventas");
        setActiveButtonConAnimacion(btnReportes);
    }

    @FXML
    void handleAlertasClick() {
        cambiarVistaConAnimacion("AlertasView.fxml", "Alertas", "Notificaciones y avisos del sistema");
        setActiveButtonConAnimacion(btnAlertas);
    }
}