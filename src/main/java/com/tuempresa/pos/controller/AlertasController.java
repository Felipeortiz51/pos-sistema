package com.tuempresa.pos.controller;

import com.tuempresa.pos.dao.ProductoDAO;
import com.tuempresa.pos.dao.VentaDAO;
import com.tuempresa.pos.model.Producto;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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

        // Cargar alertas de stock bajo
        cargarAlertasStock();

        // Cargar alertas de ventas
        cargarAlertasVentas();

        // Cargar alertas del sistema
        cargarAlertasSistema();

        // Actualizar resumen
        lblResumenAlertas.setText(totalAlertas + " alertas activas");
    }

    private void cargarAlertasStock() {
        vboxAlertasStock.getChildren().clear();

        // Obtener productos con stock bajo (menos de 5 unidades)
        List<Producto> productosStockBajo = productoDAO.getAllProductos().stream()
                .filter(p -> p.getStock() > 0 && p.getStock() <= 5)
                .toList();

        int countStock = productosStockBajo.size();
        lblCountStockBajo.setText(countStock + " productos");
        totalAlertas += countStock;

        for (Producto producto : productosStockBajo) {
            HBox alertaItem = crearAlertaItem(
                    "● " + producto.getNombre(),
                    "Stock actual: " + producto.getStock() + " unidades",
                    "hace " + calcularTiempoTranscurrido(),
                    "#dc3545"
            );
            vboxAlertasStock.getChildren().add(alertaItem);
        }

        if (productosStockBajo.isEmpty()) {
            Label sinAlertas = new Label("No hay productos con stock bajo");
            sinAlertas.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");
            vboxAlertasStock.getChildren().add(sinAlertas);
        }
    }

    private void cargarAlertasVentas() {
        vboxAlertasVentas.getChildren().clear();

        double ventasHoy = ventaDAO.sumarVentasHoy();
        double metaDiaria = 5000.0; // Meta diaria ejemplo

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

        // Por ahora, mostrar que todo está bien
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

        // Indicador de color
        Circle indicator = new Circle(4);
        indicator.setFill(Color.web(color));

        // Contenido
        VBox content = new VBox(4);
        HBox.setHgrow(content, Priority.ALWAYS);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #212529;");

        Label lblDescripcion = new Label(descripcion);
        lblDescripcion.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        content.getChildren().addAll(lblTitulo, lblDescripcion);

        // Tiempo
        Label lblTiempo = new Label(tiempo);
        lblTiempo.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        container.getChildren().addAll(indicator, content, lblTiempo);

        return container;
    }

    private String calcularTiempoTranscurrido() {
        // Simulación simple - en producción calcularías el tiempo real
        return "2 horas";
    }
}