package com.tuempresa.pos.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DashboardViewController {

    @FXML private BorderPane mainContentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnVentas;
    @FXML private Button btnProductos;
    @FXML private Button btnReportes;
    @FXML private Label lblHeaderTitle;
    @FXML private Label lblFechaHora;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        navButtons = Arrays.asList(btnDashboard, btnVentas, btnProductos, btnReportes);

        // Actualizar fecha y hora
        actualizarFechaHora();

        // Cargar vista inicial
        handleDashboardClick();
    }

    private void actualizarFechaHora() {
        if (lblFechaHora != null) {
            LocalDateTime ahora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy",
                    new Locale("es", "ES"));
            lblFechaHora.setText(ahora.format(formatter));
        }
    }

    private void cambiarVista(String fxmlFile) {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/" + fxmlFile);
            if (fxmlUrl == null) {
                System.err.println("ERROR: No se pudo encontrar el archivo FXML: " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();

            // Configurar propiedades de redimensionamiento
            if (view instanceof javafx.scene.layout.Region) {
                ((javafx.scene.layout.Region) view).setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }

            // Configuración especial para DashboardContent
            if (fxmlFile.equals("DashboardContentView.fxml")) {
                DashboardContentController contentController = loader.getController();
                contentController.setNavegacionVentasAction(event -> handleVentasClick());
            }

            mainContentArea.setCenter(view);

        } catch (IOException e) {
            System.err.println("Error de IO al cargar FXML: " + fxmlFile);
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeButton) {
        for (Button button : navButtons) {
            if (button != null) {
                button.getStyleClass().remove("nav-button-active");
                if (button.equals(activeButton)) {
                    button.getStyleClass().add("nav-button-active");
                }
            }
        }
    }

    private void actualizarTituloHeader(String titulo, String subtitulo) {
        if (lblHeaderTitle != null) {
            lblHeaderTitle.setText(titulo);
        }
        // Puedes agregar lógica para el subtítulo si tienes ese label
    }

    @FXML
    void handleDashboardClick() {
        cambiarVista("DashboardContentView.fxml");
        setActiveButton(btnDashboard);
        actualizarTituloHeader("Panel de Control", "Resumen general del negocio");
    }

    @FXML
    void handleVentasClick() {
        cambiarVista("MainView.fxml");
        setActiveButton(btnVentas);
        actualizarTituloHeader("Registrar Venta", "Punto de venta interactivo");
    }

    @FXML
    void handleProductosClick() {
        cambiarVista("ProductManagementView.fxml");
        setActiveButton(btnProductos);
        actualizarTituloHeader("Gestión de Productos", "Administrar inventario y catálogo");
    }

    @FXML
    void handleReportesClick() {
        cambiarVista("ReportsView.fxml");
        setActiveButton(btnReportes);
        actualizarTituloHeader("Reportes de Ventas", "Análisis y estadísticas del negocio");
    }
}