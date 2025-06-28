package com.tuempresa.pos.controller;

import com.tuempresa.pos.dao.ProductoDAO;
import com.tuempresa.pos.dao.VentaDAO;
import com.tuempresa.pos.model.Producto;
import com.tuempresa.pos.service.SessionManager; // <-- IMPORTANTE AÑADIR ESTA LÍNEA
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AlertasController {

    @FXML private Label lblResumenAlertas;
    @FXML private Label lblCountStockBajo;
    @FXML private Label lblCountVentas;
    @FXML private Label lblCountSistema;
    @FXML private VBox vboxAlertasStock;
    @FXML private VBox vboxAlertasVentas;
    @FXML private VBox vboxAlertasSistema;

    private ProductoDAO productoDAO;
    private VentaDAO ventaDAO;
    private int totalAlertas = 0;

    @FXML
    public void initialize() {
        productoDAO = new ProductoDAO();
        ventaDAO = new VentaDAO();

        cargarAlertas();
    }

    private void cargarAlertas() {
        totalAlertas = 0;
        cargarAlertasStock();
        cargarAlertasVentas();
        cargarAlertasSistema();
        lblResumenAlertas.setText(totalAlertas + " alertas activas");
    }

    private void cargarAlertasStock() {
        vboxAlertasStock.getChildren().clear();

        // --- INICIO DEL CAMBIO ---
        // 1. Obtenemos el ID del local activo desde la sesión.
        int idLocal = SessionManager.getInstance().getLocalId();

        // 2. Llamamos al método getAllProductos con el idLocal.
        List<Producto> productosStockBajo = productoDAO.getAllProductos(idLocal).stream()
                .filter(p -> p.getStock() > 0 && p.getStock() <= 5)
                .toList();
        // --- FIN DEL CAMBIO ---

        int countStock = productosStockBajo.size();
        lblCountStockBajo.setText(countStock + " productos");
        totalAlertas += countStock;

        for (Producto producto : productosStockBajo) {
            HBox alertaItem = crearAlertaItem(
                    "● " + producto.getNombre(),
                    "Stock actual: " + producto.getStock() + " unidades",
                    "hace 2 horas", // Simulación
                    "#dc3545"
            );
            vboxAlertasStock.getChildren().add(alertaItem);
        }

        if (productosStockBajo.isEmpty()) {
            Label sinAlertas = new Label("No hay productos con stock bajo en este local.");
            sinAlertas.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");
            vboxAlertasStock.getChildren().add(sinAlertas);
        }
    }

    private void cargarAlertasVentas() {
        vboxAlertasVentas.getChildren().clear();
        // Nota: En el futuro, este método también debería filtrar por local.
        double ventasHoy = ventaDAO.sumarVentasHoy();
        double metaDiaria = 5000.0;

        if (ventasHoy >= metaDiaria) {
            lblCountVentas.setText("Meta alcanzada");
            HBox alertaItem = crearAlertaItem(
                    "✓ Meta diaria alcanzada",
                    String.format("Ventas: $%,.2f / Meta: $%,.2f", ventasHoy, metaDiaria),
                    "Hoy",
                    "#28a745"
            );
            vboxAlertasVentas.getChildren().add(alertaItem);
        } else {
            double porcentaje = (ventasHoy / metaDiaria) * 100;
            lblCountVentas.setText(String.format("%.0f%% de la meta", porcentaje));
            HBox alertaItem = crearAlertaItem(
                    "Meta diaria pendiente",
                    String.format("Ventas: $%,.2f / Meta: $%,.2f (%.0f%%)", ventasHoy, metaDiaria, porcentaje),
                    "Hoy",
                    "#ffc107"
            );
            vboxAlertasVentas.getChildren().add(alertaItem);
            totalAlertas++;
        }
    }

    private void cargarAlertasSistema() {
        vboxAlertasSistema.getChildren().clear();
        lblCountSistema.setText("Todo en orden");
        HBox alertaItem = crearAlertaItem(
                "✓ Sistema funcionando correctamente",
                "Última verificación: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                "Ahora",
                "#0d6efd"
        );
        vboxAlertasSistema.getChildren().add(alertaItem);
    }

    private HBox crearAlertaItem(String titulo, String descripcion, String tiempo, String color) {
        HBox container = new HBox(12);
        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        container.setPadding(new Insets(12));
        container.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8px;");

        Circle indicator = new Circle(4);
        indicator.setFill(Color.web(color));

        VBox content = new VBox(4);
        HBox.setHgrow(content, Priority.ALWAYS);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #212529;");

        Label lblDescripcion = new Label(descripcion);
        lblDescripcion.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        content.getChildren().addAll(lblTitulo, lblDescripcion);

        Label lblTiempo = new Label(tiempo);
        lblTiempo.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        container.getChildren().addAll(indicator, content, lblTiempo);

        return container;
    }
}