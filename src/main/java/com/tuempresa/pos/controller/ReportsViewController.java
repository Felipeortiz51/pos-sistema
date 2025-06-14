package com.tuempresa.pos.controller;

import com.tuempresa.pos.dao.VentaDAO;
import com.tuempresa.pos.model.Venta;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsViewController {

    @FXML private DatePicker datePickerInicio;
    @FXML private DatePicker datePickerFin;
    @FXML private Button btnGenerarReporte;
    @FXML private TableView<Venta> tablaReporteVentas;
    @FXML private TableColumn<Venta, Integer> colIdVenta;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private TableColumn<Venta, Double> colTotal;
    @FXML private TableColumn<Venta, Integer> colNumItems;
    @FXML private Label lblVentasTotales;
    @FXML private Label lblNumeroTransacciones;
    @FXML private Label lblNumeroTransaccionesSummary; // Nuevo label
    @FXML private Label lblPromedioVenta; // Nuevo label

    private VentaDAO ventaDAO;

    @FXML
    public void initialize() {
        ventaDAO = new VentaDAO();

        // Establecer fechas por defecto: el mes actual
        datePickerInicio.setValue(LocalDate.now().withDayOfMonth(1));
        datePickerFin.setValue(LocalDate.now());

        configurarColumnas();
    }

    private void configurarColumnas() {
        colIdVenta.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        colTotal.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());

        // Formatear la fecha para que se vea bien
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colFecha.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFechaVenta().format(formatter))
        );

        // Calcular el número de items de cada venta
        colNumItems.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getDetalles().size()).asObject()
        );

        // Formatear columnas de números
        colTotal.setCellFactory(col -> new TableCell<Venta, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%,.2f", item));
                }
            }
        });
    }

    @FXML
    private void handleGenerarReporte() {
        LocalDate inicio = datePickerInicio.getValue();
        LocalDate fin = datePickerFin.getValue();

        if (inicio == null || fin == null) {
            mostrarAlerta("Error", "Por favor, seleccione una fecha de inicio y una fecha de fin.");
            return;
        }

        if (inicio.isAfter(fin)) {
            mostrarAlerta("Error", "La fecha de inicio no puede ser posterior a la fecha de fin.");
            return;
        }

        List<Venta> ventas = ventaDAO.getVentasPorRangoDeFechas(inicio, fin);
        tablaReporteVentas.setItems(FXCollections.observableArrayList(ventas));

        // Calcular y mostrar los totales
        double totalVentas = ventas.stream().mapToDouble(Venta::getTotal).sum();
        int numeroTransacciones = ventas.size();
        double promedioVenta = numeroTransacciones > 0 ? totalVentas / numeroTransacciones : 0.0;

        // Actualizar todos los labels
        lblVentasTotales.setText(String.format("$%,.2f", totalVentas));
        lblNumeroTransacciones.setText(String.format("%d transacciones", numeroTransacciones));

        // Actualizar los nuevos labels si existen
        if (lblNumeroTransaccionesSummary != null) {
            lblNumeroTransaccionesSummary.setText(String.valueOf(numeroTransacciones));
        }
        if (lblPromedioVenta != null) {
            lblPromedioVenta.setText(String.format("$%,.2f", promedioVenta));
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        // Aplicar estilo minimalista al diálogo
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/dashboard-styles.css").toExternalForm()
        );

        alert.showAndWait();
    }
}