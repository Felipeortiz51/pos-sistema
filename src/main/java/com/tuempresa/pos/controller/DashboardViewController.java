package com.tuempresa.pos.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class DashboardViewController {

    @FXML private BorderPane mainContentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnVentas;
    @FXML private Button btnProductos;
    @FXML private Button btnReportes;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        navButtons = Arrays.asList(btnDashboard, btnVentas, btnProductos, btnReportes);
        handleDashboardClick();
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

            if (view instanceof javafx.scene.layout.Region) {
                ((javafx.scene.layout.Region) view).setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }

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

    @FXML
    void handleDashboardClick() {
        cambiarVista("DashboardContentView.fxml");
        setActiveButton(btnDashboard);
    }

    @FXML
    void handleVentasClick() {
        cambiarVista("MainView.fxml");
        setActiveButton(btnVentas);
    }

    @FXML
    void handleProductosClick() {
        cambiarVista("ProductManagementView.fxml");
        setActiveButton(btnProductos);
    }
    @FXML
    void handleReportesClick() {
        cambiarVista("ReportsView.fxml");
        setActiveButton(btnReportes);
    }
}