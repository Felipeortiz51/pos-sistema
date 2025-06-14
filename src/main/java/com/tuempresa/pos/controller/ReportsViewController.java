package com.tuempresa.pos.controller;

import com.tuempresa.pos.dao.VentaDAO;
import com.tuempresa.pos.model.Venta;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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
        colIdVenta.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Formatear la fecha para que se vea bien
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        colFecha.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFechaVenta().format(formatter))
        );

        // Calcular el número de items de cada venta
        colNumItems.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getDetalles().size()).asObject()
        );
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
        lblVentasTotales.setText(String.format("$%,.2f", totalVentas));
        lblNumeroTransacciones.setText(String.valueOf(ventas.size()));
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}