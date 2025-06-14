package com.tuempresa.pos.controller;

import com.tuempresa.pos.dao.ProductoDAO;
import com.tuempresa.pos.dao.VentaDAO;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DashboardContentController {

    @FXML private Label lblProductosActivos;
    @FXML private Label lblVentasHoy;
    @FXML private Label lblVentasSubtitulo;
    @FXML private Label lblAlertasStock;
    @FXML private Button btnAccesoRapidoVentas;
    @FXML private BarChart<String, Number> barChartVentas; // Nuevo FXML id para el gráfico

    private ProductoDAO productoDAO;
    private VentaDAO ventaDAO;

    @FXML
    public void initialize() {
        productoDAO = new ProductoDAO();
        ventaDAO = new VentaDAO();

        cargarDatosDashboard();
        cargarDatosGrafico(); // Nuevo método para el gráfico
    }

    public void setNavegacionVentasAction(EventHandler<ActionEvent> action) {
        if (btnAccesoRapidoVentas != null) {
            btnAccesoRapidoVentas.setOnAction(action);
        }
    }

    private void cargarDatosDashboard() {
        // Lógica para las tarjetas de datos...
        int productos = productoDAO.contarProductosActivos();
        lblProductosActivos.setText(String.valueOf(productos));

        double ventasHoy = ventaDAO.sumarVentasHoy();
        lblVentasHoy.setText(String.format("$%,.2f", ventasHoy));

        int alertas = productoDAO.contarAlertasStock(5);
        lblAlertasStock.setText(String.valueOf(alertas));

        double ventasAyer = ventaDAO.sumarVentasAyer();
        String textoSubtitulo;
        double porcentajeCambio = 0;

        lblVentasSubtitulo.getStyleClass().removeAll("card-subtitle", "card-subtitle-alert");

        if (ventasAyer > 0) {
            porcentajeCambio = ((ventasHoy - ventasAyer) / ventasAyer) * 100;
            if (porcentajeCambio >= 0) {
                textoSubtitulo = String.format("+%.0f%% que ayer", porcentajeCambio);
                lblVentasSubtitulo.getStyleClass().add("card-subtitle");
            } else {
                textoSubtitulo = String.format("%.0f%% que ayer", porcentajeCambio);
                lblVentasSubtitulo.getStyleClass().add("card-subtitle-alert");
            }
        } else if (ventasHoy > 0) {
            textoSubtitulo = "Primeras ventas";
            lblVentasSubtitulo.getStyleClass().add("card-subtitle");
        } else {
            textoSubtitulo = "Sin movimiento";
            lblVentasSubtitulo.getStyleClass().add("card-subtitle");
        }
        lblVentasSubtitulo.setText(textoSubtitulo);
    }

    /**
     * Nuevo método para obtener los datos y construir el gráfico de barras.
     */
    private void cargarDatosGrafico() {
        // Obtenemos el mapa de ventas por día desde el DAO
        Map<LocalDate, Double> ventasPorDia = ventaDAO.getVentasDeUltimos7Dias();

        // Creamos una "serie" de datos, que es lo que el gráfico entiende
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventas Diarias");

        // Definimos un formateador para mostrar las fechas de forma amigable (ej. "14-06")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM");

        // Iteramos por los últimos 7 días para asegurarnos de que todos aparezcan,
        // incluso si un día no tuvo ventas.
        for (int i = 6; i >= 0; i--) {
            LocalDate fecha = LocalDate.now().minusDays(i);

            // Usamos getOrDefault para obtener 0.0 si un día no está en el mapa (no hubo ventas)
            double ventas = ventasPorDia.getOrDefault(fecha, 0.0);

            // Añadimos una barra al gráfico con la fecha formateada y el total de ventas
            series.getData().add(new XYChart.Data<>(fecha.format(formatter), ventas));
        }

        // Limpiamos datos antiguos y añadimos la nueva serie al gráfico
        barChartVentas.getData().clear();
        barChartVentas.getData().add(series);
    }
}