package com.tuempresa.pos.controller;

import com.tuempresa.pos.dao.ProductoDAO;
import com.tuempresa.pos.dao.VentaDAO;
import com.tuempresa.pos.model.DetalleVenta;
import com.tuempresa.pos.model.Producto;
import com.tuempresa.pos.model.Venta;
import com.tuempresa.pos.util.NotificationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class MainViewController {

    @FXML private TextField txtCodigoBarras;
    @FXML private TextField txtNombreProducto;
    @FXML private Spinner<Integer> spinnerCantidad;
    @FXML private Label lblTotal;
    @FXML private TableView<DetalleVenta> tablaVenta;
    @FXML private TableColumn<DetalleVenta, String> colProducto;
    @FXML private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML private TableColumn<DetalleVenta, Double> colPrecioUnitario;
    @FXML private TableColumn<DetalleVenta, Double> colSubtotal;
    @FXML private TableColumn<DetalleVenta, Void> colAcciones;

    private ProductoDAO productoDAO;
    private VentaDAO ventaDAO;
    private ObservableList<DetalleVenta> carrito;
    private Producto productoSeleccionado;
    private ObservableList<Producto> listaProductosCompleta;

    @FXML
    public void initialize() {
        productoDAO = new ProductoDAO();
        ventaDAO = new VentaDAO();
        carrito = FXCollections.observableArrayList();
        listaProductosCompleta = FXCollections.observableArrayList(productoDAO.getAllProductos());
        configurarTabla();
        tablaVenta.setItems(carrito);
        setupAutocomplete();
    }

    /**
     * ===================================================================================
     * MÉTODO DEFINITIVO Y CORREGIDO PARA FUNCIONAR CON LA BIBLIOTECA CONTROLSFX v11.2.2
     * ===================================================================================
     */
    private void setupAutocomplete() {
        // 1. Definimos el convertidor que le dice a la lista cómo mostrar un objeto Producto.
        StringConverter<Producto> converter = new StringConverter<>() {
            @Override
            public String toString(Producto producto) {
                // Si el objeto producto no es nulo, muestra su nombre. Si no, muestra un texto vacío.
                return (producto == null) ? "" : producto.getNombre();
            }

            @Override
            public Producto fromString(String string) {
                // Esta parte no es necesaria para nosotros, porque seleccionamos de una lista.
                return null;
            }
        };

        // 2. Usamos la sintaxis correcta para la v11, pasando el 'converter' como TERCER argumento.
        AutoCompletionBinding<Producto> autoCompletionBinding = TextFields.bindAutoCompletion(
                txtNombreProducto, // El campo de texto donde se escribe
                suggestionRequest -> { // La lógica para generar sugerencias
                    String query = suggestionRequest.getUserText().toLowerCase();
                    return listaProductosCompleta.stream().filter(producto ->
                            producto.getNombre().toLowerCase().contains(query)
                    ).collect(Collectors.toList());
                },
                converter // El convertidor que creamos arriba
        );

        // 3. Definimos qué hacer cuando el usuario selecciona un item de la lista de sugerencias.
        autoCompletionBinding.setOnAutoCompleted(event -> {
            productoSeleccionado = event.getCompletion();
            // Rellenamos los campos con los datos del producto seleccionado
            txtCodigoBarras.setText(productoSeleccionado.getCodigoBarras());
            txtNombreProducto.setText(productoSeleccionado.getNombre());
            spinnerCantidad.requestFocus();
        });
    }

    private void configurarTabla() {
        colProducto.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        agregarBotonEliminar();
    }

    @FXML
    private void buscarProductoPorCodigo() {
        String codigo = txtCodigoBarras.getText().trim();
        if (codigo.isEmpty()) {
            return;
        }

        productoSeleccionado = productoDAO.buscarPorCodigo(codigo);
        if (productoSeleccionado != null) {
            txtNombreProducto.setText(productoSeleccionado.getNombre());
            spinnerCantidad.getValueFactory().setValue(1);
            agregarAlCarrito();
        } else {
            NotificationUtil.mostrarAdvertencia("No encontrado", "Producto con código " + codigo + " no encontrado.");
            txtNombreProducto.clear();
        }
    }

    @FXML
    private void agregarAlCarrito() {
        if (productoSeleccionado == null) {
            NotificationUtil.mostrarAdvertencia("Sin producto", "Busca y selecciona un producto primero.");
            return;
        }
        int cantidad = spinnerCantidad.getValue();
        if (cantidad > productoSeleccionado.getStock()) {
            NotificationUtil.mostrarError("Error de Stock", "No hay suficiente stock. Disponible: " + productoSeleccionado.getStock());
            return;
        }

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(productoSeleccionado);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(productoSeleccionado.getPrecio());

        carrito.add(detalle);
        actualizarTotal();
        limpiarCamposProducto();
    }

    @FXML
    private void finalizarVenta() {
        if (carrito.isEmpty()) {
            NotificationUtil.mostrarError("Error", "El carrito está vacío. No se puede registrar la venta.");
            return;
        }
        Venta nuevaVenta = new Venta();
        nuevaVenta.setDetalles(new ArrayList<>(carrito));
        nuevaVenta.setTotal(calcularTotal());

        boolean exito = ventaDAO.registrarVenta(nuevaVenta);
        if (exito) {
            NotificationUtil.mostrarInformacion("Venta Exitosa", "La venta se ha registrado correctamente.");
            cancelarVenta();
        } else {
            NotificationUtil.mostrarError("Error de Venta", "Ocurrió un error y la venta no pudo ser registrada. Revise el stock e intente de nuevo.");
        }
    }

    @FXML
    private void cancelarVenta() {
        carrito.clear();
        actualizarTotal();
        limpiarCamposProducto();
    }

    private void agregarBotonEliminar() {
        Callback<TableColumn<DetalleVenta, Void>, TableCell<DetalleVenta, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btn = new Button("Eliminar");
            {
                btn.setStyle("-fx-background-color: #e76f51; -fx-text-fill: white;");
                btn.setOnAction(event -> {
                    DetalleVenta detalle = getTableView().getItems().get(getIndex());
                    carrito.remove(detalle);
                    actualizarTotal();
                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        };
        colAcciones.setCellFactory(cellFactory);
    }

    private double calcularTotal() {
        return carrito.stream().mapToDouble(DetalleVenta::getSubtotal).sum();
    }

    private void actualizarTotal() {
        lblTotal.setText(String.format("$%,.2f", calcularTotal()));
    }

    private void limpiarCamposProducto() {
        productoSeleccionado = null;
        txtCodigoBarras.clear();
        txtNombreProducto.clear();
        spinnerCantidad.getValueFactory().setValue(1);
        txtCodigoBarras.requestFocus();
    }
}