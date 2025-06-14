package com.tuempresa.pos.controller;

import com.tuempresa.pos.dao.ProductoDAO;
import com.tuempresa.pos.model.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.tuempresa.pos.util.NotificationUtil;

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

        // Añadir un listener para cuando se selecciona un producto en la tabla


        cargarProductos();

        FilteredList<Producto> listaFiltrada = new FilteredList<>(listaProductosMaster, p -> true);
        txtBusqueda.textProperty().addListener((observable, oldValue, newValue) -> {
            listaFiltrada.setPredicate(producto -> {
                // Si el campo de búsqueda está vacío, muestra todos los productos.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String textoBusqueda = newValue.toLowerCase();

                // Compara el nombre y el código de barras con el texto de búsqueda.
                if (producto.getNombre().toLowerCase().contains(textoBusqueda)) {
                    return true; // Coincide por nombre.
                } else if (producto.getCodigoBarras().toLowerCase().contains(textoBusqueda)) {
                    return true; // Coincide por código de barras.
                }

                return false; // No hay coincidencia.
            });
        });
        SortedList<Producto> listaOrdenada = new SortedList<>(listaFiltrada);
        listaOrdenada.comparatorProperty().bind(tablaProductos.comparatorProperty());
        tablaProductos.setItems(listaOrdenada);
        tablaProductos.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> mostrarDetallesProducto(newValue)
        );

    }


    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCodigoBarras.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
    }

    private void configurarSpinners() {
        // Configuración para el Spinner de Precio (Double)
        SpinnerValueFactory<Double> precioFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1000000.0, 0.0, 50.0);
        spinnerPrecio.setValueFactory(precioFactory);

        // Configuración para el Spinner de Stock (Integer)
        SpinnerValueFactory<Integer> stockFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0);
        spinnerStock.setValueFactory(stockFactory);
    }

    private void cargarProductos() {
        // Ahora este método solo carga los datos en la lista "maestra"
        listaProductosMaster.setAll(productoDAO.getAllProductos());
    }

    private void mostrarDetallesProducto(Producto producto) {
        if (producto != null) {
            // Rellenar el formulario con los datos del producto seleccionado
            txtNombre.setText(producto.getNombre());
            txtCodigoBarras.setText(producto.getCodigoBarras());
            spinnerPrecio.getValueFactory().setValue(producto.getPrecio());
            spinnerStock.getValueFactory().setValue(producto.getStock());
            txtDescripcion.setText(producto.getDescripcion());
        } else {
            // Limpiar el formulario si no hay nada seleccionado
            limpiarFormulario();
        }
    }

    @FXML
    private void handleGuardarProducto() {
        // Obtener el producto seleccionado de la tabla
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();

        if (txtNombre.getText().isEmpty() || txtCodigoBarras.getText().isEmpty()) {
            NotificationUtil.mostrarError("Error", "Los campos Nombre y Código de Barras son obligatorios.");
            return;
        }

        boolean exito;

        if (productoSeleccionado == null) { // Modo: Crear nuevo producto
            Producto nuevoProducto = new Producto();
            nuevoProducto.setNombre(txtNombre.getText());
            nuevoProducto.setCodigoBarras(txtCodigoBarras.getText());
            nuevoProducto.setPrecio(spinnerPrecio.getValue());
            nuevoProducto.setStock(spinnerStock.getValue());
            nuevoProducto.setDescripcion(txtDescripcion.getText());

            exito = productoDAO.crearProducto(nuevoProducto);
        } else { // Modo: Actualizar producto existente
            productoSeleccionado.setNombre(txtNombre.getText());
            productoSeleccionado.setCodigoBarras(txtCodigoBarras.getText());
            productoSeleccionado.setPrecio(spinnerPrecio.getValue());
            productoSeleccionado.setStock(spinnerStock.getValue());
            productoSeleccionado.setDescripcion(txtDescripcion.getText());

            exito = productoDAO.actualizarProducto(productoSeleccionado);
        }

        if (exito) {
            cargarProductos(); // Recargar la tabla para mostrar los cambios
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