package com.tuempresa.pos.controller;

import com.tuempresa.pos.dao.ProductoDAO;
import com.tuempresa.pos.model.Producto;
import com.tuempresa.pos.service.SessionManager;
import com.tuempresa.pos.util.NotificationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class ProductManagementController {

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCodigoBarras;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;

    @FXML private TextField txtNombre;
    @FXML private TextField txtCodigoBarras;
    @FXML private Spinner<Double> spinnerPrecio;
    @FXML private Spinner<Integer> spinnerStock;
    @FXML private TextField txtDescripcion;
    @FXML private Button btnGuardar;
    @FXML private Button btnNuevo;
    @FXML private Button btnEliminar;
    @FXML private TextField txtBusqueda;

    private ProductoDAO productoDAO;
    private ObservableList<Producto> listaProductosMaster;

    @FXML
    public void initialize() {
        productoDAO = new ProductoDAO();
        listaProductosMaster = FXCollections.observableArrayList();

        configurarColumnas();
        configurarSpinners();
        cargarProductos();
        configurarBusqueda();

        tablaProductos.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> mostrarDetallesProducto(newValue)
        );

        // Aplicar permisos según el rol del usuario logueado
        aplicarPermisos();
    }

    private void aplicarPermisos() {
        boolean esAdmin = SessionManager.getInstance().isAdmin();

        // Deshabilitar los campos del formulario si el usuario no es admin
        txtNombre.setDisable(!esAdmin);
        txtCodigoBarras.setDisable(!esAdmin);
        spinnerPrecio.setDisable(!esAdmin);
        spinnerStock.setDisable(!esAdmin);
        txtDescripcion.setDisable(!esAdmin);

        // Ocultar los botones de acción si no es admin
        btnGuardar.setVisible(esAdmin);
        btnGuardar.setManaged(esAdmin); // Con setManaged(false) el botón no ocupa espacio
        btnNuevo.setVisible(esAdmin);
        btnNuevo.setManaged(esAdmin);
        btnEliminar.setVisible(esAdmin);
        btnEliminar.setManaged(esAdmin);

        if (!esAdmin) {
            limpiarFormulario(); // Limpiamos por si había algo seleccionado
            txtBusqueda.setPromptText("Búsqueda (solo vista)");
        }
    }

    private void configurarBusqueda() {
        FilteredList<Producto> listaFiltrada = new FilteredList<>(listaProductosMaster, p -> true);
        txtBusqueda.textProperty().addListener((observable, oldValue, newValue) -> {
            listaFiltrada.setPredicate(producto -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String textoBusqueda = newValue.toLowerCase();
                if (producto.getNombre().toLowerCase().contains(textoBusqueda)) {
                    return true;
                } else return producto.getCodigoBarras().toLowerCase().contains(textoBusqueda);
            });
        });
        SortedList<Producto> listaOrdenada = new SortedList<>(listaFiltrada);
        listaOrdenada.comparatorProperty().bind(tablaProductos.comparatorProperty());
        tablaProductos.setItems(listaOrdenada);
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCodigoBarras.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
    }

    private void configurarSpinners() {
        SpinnerValueFactory<Double> precioFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1000000.0, 0.0, 50.0);
        spinnerPrecio.setValueFactory(precioFactory);
        SpinnerValueFactory<Integer> stockFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0);
        spinnerStock.setValueFactory(stockFactory);
    }

    private void cargarProductos() {
        listaProductosMaster.setAll(productoDAO.getAllProductos());
    }

    private void mostrarDetallesProducto(Producto producto) {
        if (producto != null) {
            txtNombre.setText(producto.getNombre());
            txtCodigoBarras.setText(producto.getCodigoBarras());
            spinnerPrecio.getValueFactory().setValue(producto.getPrecio());
            spinnerStock.getValueFactory().setValue(producto.getStock());
            txtDescripcion.setText(producto.getDescripcion());
        } else {
            limpiarFormulario();
        }
    }

    @FXML
    private void handleGuardarProducto() {
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (txtNombre.getText().isEmpty() || txtCodigoBarras.getText().isEmpty()) {
            NotificationUtil.mostrarError("Error", "Los campos Nombre y Código de Barras son obligatorios.");
            return;
        }
        boolean exito;
        if (productoSeleccionado == null) {
            Producto nuevoProducto = new Producto();
            nuevoProducto.setNombre(txtNombre.getText());
            nuevoProducto.setCodigoBarras(txtCodigoBarras.getText());
            nuevoProducto.setPrecio(spinnerPrecio.getValue());
            nuevoProducto.setStock(spinnerStock.getValue());
            nuevoProducto.setDescripcion(txtDescripcion.getText());
            exito = productoDAO.crearProducto(nuevoProducto);
        } else {
            productoSeleccionado.setNombre(txtNombre.getText());
            productoSeleccionado.setCodigoBarras(txtCodigoBarras.getText());
            productoSeleccionado.setPrecio(spinnerPrecio.getValue());
            productoSeleccionado.setStock(spinnerStock.getValue());
            productoSeleccionado.setDescripcion(txtDescripcion.getText());
            exito = productoDAO.actualizarProducto(productoSeleccionado);
        }
        if (exito) {
            cargarProductos();
            limpiarFormulario();
            tablaProductos.getSelectionModel().clearSelection();
        } else {
            NotificationUtil.mostrarError("Error", "No se pudo guardar el producto en la base de datos.");
        }
    }

    @FXML
    private void handleNuevoProducto() {
        tablaProductos.getSelectionModel().clearSelection();
        limpiarFormulario();
        txtNombre.requestFocus();
    }

    @FXML
    private void handleEliminarProducto() {
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado == null) {
            NotificationUtil.mostrarError("Error", "Por favor, seleccione un producto de la tabla para eliminar.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Está seguro de que desea eliminar el producto: " + productoSeleccionado.getNombre() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean exito = productoDAO.eliminarProducto(productoSeleccionado.getId());
            if (exito) {
                cargarProductos();
                limpiarFormulario();
            } else {
                NotificationUtil.mostrarError("Error", "No se pudo eliminar el producto.");
            }
        }
    }
    @FXML
    private void buscarProductoPorCodigo() {
        String codigo = txtCodigoBarras.getText().trim();
        if (codigo.isEmpty()) {
            // Si el campo está vacío, simplemente lo limpiamos y ya
            handleNuevoProducto();
            return;
        }

        Producto productoEncontrado = productoDAO.buscarPorCodigo(codigo);

        if (productoEncontrado != null) {
            // Si el producto existe, lo seleccionamos en la tabla
            tablaProductos.getSelectionModel().select(productoEncontrado);
            // Hacemos que la tabla se desplace hasta el producto para que sea visible
            tablaProductos.scrollTo(productoEncontrado);
        } else {
            // Si el producto NO existe, limpiamos el formulario pero mantenemos el
            // código de barras para que el usuario pueda crear un nuevo producto con él.
            handleNuevoProducto(); // Esto limpia el formulario
            txtCodigoBarras.setText(codigo); // Restauramos el código escaneado
            txtNombre.requestFocus(); // Ponemos el foco en el campo "Nombre"
        }
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtCodigoBarras.clear();
        spinnerPrecio.getValueFactory().setValue(0.0);
        spinnerStock.getValueFactory().setValue(0);
        txtDescripcion.clear();
    }
}