package com.tuempresa.pos.controller;

import com.tuempresa.pos.dao.ProductoDAO;
import com.tuempresa.pos.dao.VentaDAO;
import com.tuempresa.pos.model.DetalleVenta;
import com.tuempresa.pos.model.PagoCompleto; // <-- IMPORTANTE
import com.tuempresa.pos.model.Producto;
import com.tuempresa.pos.model.Venta;
import com.tuempresa.pos.service.SessionManager;
import com.tuempresa.pos.util.NotificationUtil;
import javafx.animation.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // <-- IMPORTANTE
import javafx.scene.Parent; // <-- IMPORTANTE
import javafx.scene.Scene; // <-- IMPORTANTE
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.Modality; // <-- IMPORTANTE
import javafx.stage.Stage; // <-- IMPORTANTE
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException; // <-- IMPORTANTE
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.function.Consumer; // <-- IMPORTANTE
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
        // --- CAMBIO 1: Cargar productos del local activo ---
        // Obtenemos el ID del local de la sesión
        int idLocal = SessionManager.getInstance().getLocalId();
        // Llamamos al método actualizado del DAO pasándole el ID del local
        listaProductosCompleta = FXCollections.observableArrayList(productoDAO.getAllProductos(idLocal));

        configurarTabla();
        tablaVenta.setItems(carrito);
        setupAutocomplete();

        // Configurar animaciones
        animarEntradaVentas();
        setupFieldAnimations();
    }

    /**
     * Animación de entrada de la vista de ventas
     */
    private void animarEntradaVentas() {
        // Solo animar si los elementos existen y tienen un parent
        if (txtCodigoBarras != null && txtCodigoBarras.getParent() != null) {
            // Animar entrada del formulario lateral
            javafx.scene.Node formulario = txtCodigoBarras.getParent().getParent();
            if (formulario != null) {
                formulario.setTranslateX(-300);
                formulario.setOpacity(0);

                TranslateTransition slideForm = new TranslateTransition(Duration.millis(600), formulario);
                slideForm.setFromX(-300);
                slideForm.setToX(0);
                slideForm.setInterpolator(Interpolator.EASE_OUT);

                FadeTransition fadeForm = new FadeTransition(Duration.millis(600), formulario);
                fadeForm.setFromValue(0);
                fadeForm.setToValue(1);

                ParallelTransition formEntry = new ParallelTransition(slideForm, fadeForm);
                formEntry.play();
            }
        }

        // Animar entrada de la tabla
        if (tablaVenta != null) {
            tablaVenta.setTranslateX(300);
            tablaVenta.setOpacity(0);

            TranslateTransition slideTable = new TranslateTransition(Duration.millis(600), tablaVenta);
            slideTable.setFromX(300);
            slideTable.setToX(0);
            slideTable.setDelay(Duration.millis(200));
            slideTable.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fadeTable = new FadeTransition(Duration.millis(600), tablaVenta);
            fadeTable.setFromValue(0);
            fadeTable.setToValue(1);
            fadeTable.setDelay(Duration.millis(200));

            ParallelTransition tableEntry = new ParallelTransition(slideTable, fadeTable);
            tableEntry.play();
        }
    }

    /**
     * Configurar animaciones para campos de entrada
     */
    private void setupFieldAnimations() {
        // Animación de focus para código de barras
        if (txtCodigoBarras != null) {
            txtCodigoBarras.focusedProperty().addListener((obs, oldVal, newVal) -> {
                animateFieldFocus(txtCodigoBarras, newVal, Color.web("#28a745"));
            });
        }

        // Animación de focus para nombre de producto
        if (txtNombreProducto != null) {
            txtNombreProducto.focusedProperty().addListener((obs, oldVal, newVal) -> {
                animateFieldFocus(txtNombreProducto, newVal, Color.web("#0d6efd"));
            });
        }

        // Animación de focus para spinner
        if (spinnerCantidad != null) {
            spinnerCantidad.focusedProperty().addListener((obs, oldVal, newVal) -> {
                animateFieldFocus(spinnerCantidad, newVal, Color.web("#ffc107"));
            });
        }
    }

    /**
     * Animar focus de campos
     */
    private void animateFieldFocus(javafx.scene.control.Control field, boolean focused, Color glowColor) {
        if (field == null) return;

        if (focused) {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), field);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();

            DropShadow glow = new DropShadow();
            glow.setColor(glowColor.deriveColor(0, 1, 1, 0.6));
            glow.setRadius(10);
            field.setEffect(glow);
        } else {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), field);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();

            field.setEffect(null);
        }
    }

    /**
     * Configurar autocompletado
     */
    private void setupAutocomplete() {
        if (txtNombreProducto == null || listaProductosCompleta == null) return;

        StringConverter<Producto> converter = new StringConverter<>() {
            @Override
            public String toString(Producto producto) {
                return (producto == null) ? "" : producto.getNombre();
            }

            @Override
            public Producto fromString(String string) {
                return null;
            }
        };

        try {
            AutoCompletionBinding<Producto> autoCompletionBinding = TextFields.bindAutoCompletion(
                    txtNombreProducto,
                    suggestionRequest -> {
                        String query = suggestionRequest.getUserText().toLowerCase();
                        return listaProductosCompleta.stream().filter(producto ->
                                producto.getNombre().toLowerCase().contains(query)
                        ).collect(Collectors.toList());
                    },
                    converter
            );

            autoCompletionBinding.setOnAutoCompleted(event -> {
                productoSeleccionado = event.getCompletion();
                if (txtCodigoBarras != null) {
                    txtCodigoBarras.setText(productoSeleccionado.getCodigoBarras());
                }
                txtNombreProducto.setText(productoSeleccionado.getNombre());

                // Animación de confirmación de selección
                animarSeleccionProducto();

                if (spinnerCantidad != null) {
                    spinnerCantidad.requestFocus();
                }
            });
        } catch (Exception e) {
            System.err.println("Error configurando autocompletado: " + e.getMessage());
        }
    }

    /**
     * Animación cuando se selecciona un producto
     */
    private void animarSeleccionProducto() {
        // Efecto de "flash" verde para confirmar la selección
        DropShadow greenFlash = new DropShadow();
        greenFlash.setColor(Color.web("#28a745", 0.8));
        greenFlash.setRadius(15);

        if (txtNombreProducto != null) {
            txtNombreProducto.setEffect(greenFlash);
        }
        if (txtCodigoBarras != null) {
            txtCodigoBarras.setEffect(greenFlash);
        }

        Timeline removeFlash = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            if (txtNombreProducto != null) txtNombreProducto.setEffect(null);
            if (txtCodigoBarras != null) txtCodigoBarras.setEffect(null);
        }));
        removeFlash.play();
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
        animarBusqueda(txtCodigoBarras);
        // --- CAMBIO 2: Buscar producto por código en el local activo ---
        int idLocal = SessionManager.getInstance().getLocalId();
        productoSeleccionado = productoDAO.buscarPorCodigo(codigo, idLocal);

        if (productoSeleccionado != null) {
            txtNombreProducto.setText(productoSeleccionado.getNombre());
            spinnerCantidad.getValueFactory().setValue(1);
            animarBusquedaExitosa();
            agregarAlCarrito();
        } else {
            animarBusquedaFallida();
            NotificationUtil.mostrarAdvertencia("No encontrado", "Producto con código " + codigo + " no encontrado.");
            txtNombreProducto.clear();
        }
    }

    /**
     * Animación durante la búsqueda
     */
    private void animarBusqueda(TextField campo) {
        if (campo == null) return;

        FadeTransition loading = new FadeTransition(Duration.millis(300), campo);
        loading.setFromValue(1.0);
        loading.setToValue(0.7);
        loading.setCycleCount(2);
        loading.setAutoReverse(true);
        loading.play();
    }

    /**
     * Animación de búsqueda exitosa
     */
    private void animarBusquedaExitosa() {
        if (txtNombreProducto == null) return;

        ScaleTransition success = new ScaleTransition(Duration.millis(200), txtNombreProducto);
        success.setFromX(1.0);
        success.setFromY(1.0);
        success.setToX(1.05);
        success.setToY(1.05);
        success.setCycleCount(2);
        success.setAutoReverse(true);

        DropShadow successGlow = new DropShadow();
        successGlow.setColor(Color.web("#28a745", 0.6));
        successGlow.setRadius(12);
        txtNombreProducto.setEffect(successGlow);

        success.setOnFinished(e -> {
            Timeline removeEffect = new Timeline(new KeyFrame(Duration.millis(800), ev -> {
                txtNombreProducto.setEffect(null);
            }));
            removeEffect.play();
        });

        success.play();
    }

    /**
     * Animación de búsqueda fallida
     */
    private void animarBusquedaFallida() {
        if (txtCodigoBarras == null) return;

        TranslateTransition shake = new TranslateTransition(Duration.millis(50), txtCodigoBarras);
        shake.setFromX(0);
        shake.setByX(5);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);

        DropShadow errorGlow = new DropShadow();
        errorGlow.setColor(Color.web("#dc3545", 0.6));
        errorGlow.setRadius(12);
        txtCodigoBarras.setEffect(errorGlow);

        shake.setOnFinished(e -> {
            Timeline removeEffect = new Timeline(new KeyFrame(Duration.millis(1000), ev -> {
                txtCodigoBarras.setEffect(null);
            }));
            removeEffect.play();
        });

        shake.play();
    }

    @FXML
    private void agregarAlCarrito() {
        if (productoSeleccionado == null) {
            NotificationUtil.mostrarAdvertencia("Sin producto", "Busca y selecciona un producto primero.");
            return;
        }

        int cantidad = spinnerCantidad.getValue();
        if (cantidad > productoSeleccionado.getStock()) {
            animarErrorStock();
            NotificationUtil.mostrarError("Error de Stock", "No hay suficiente stock. Disponible: " + productoSeleccionado.getStock());
            return;
        }

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(productoSeleccionado);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(productoSeleccionado.getPrecio());

        carrito.add(detalle);
        animarProductoAgregado();
        actualizarTotalConAnimacion();
        limpiarCamposProducto();
    }

    /**
     * Animación de error de stock
     */
    private void animarErrorStock() {
        if (spinnerCantidad == null) return;

        TranslateTransition shake = new TranslateTransition(Duration.millis(50), spinnerCantidad);
        shake.setFromX(0);
        shake.setByX(3);
        shake.setCycleCount(8);
        shake.setAutoReverse(true);

        DropShadow errorGlow = new DropShadow();
        errorGlow.setColor(Color.web("#dc3545", 0.8));
        errorGlow.setRadius(15);
        spinnerCantidad.setEffect(errorGlow);

        shake.setOnFinished(e -> {
            Timeline removeEffect = new Timeline(new KeyFrame(Duration.seconds(2), ev -> {
                spinnerCantidad.setEffect(null);
            }));
            removeEffect.play();
        });

        shake.play();
    }

    /**
     * Animación cuando se agrega un producto al carrito
     */
    private void animarProductoAgregado() {
        if (txtNombreProducto == null || tablaVenta == null) return;

        // Animar la nueva fila en la tabla
        Timeline animarFila = new Timeline(new KeyFrame(Duration.millis(400), e -> {
            if (!tablaVenta.getItems().isEmpty()) {
                int ultimaFila = tablaVenta.getItems().size() - 1;
                tablaVenta.scrollTo(ultimaFila);

                // Highlight de la fila nueva
                Timeline highlight = new Timeline(new KeyFrame(Duration.millis(100), ev -> {
                    tablaVenta.getSelectionModel().select(ultimaFila);
                }));

                Timeline unhighlight = new Timeline(new KeyFrame(Duration.millis(1000), ev -> {
                    tablaVenta.getSelectionModel().clearSelection();
                }));

                SequentialTransition highlightSequence = new SequentialTransition(highlight, unhighlight);
                highlightSequence.play();
            }
        }));
        animarFila.play();
    }

    /**
     * Actualizar total con animación
     */
    private void actualizarTotalConAnimacion() {
        if (lblTotal == null) return;

        double nuevoTotal = calcularTotal();

        // Si el carrito está vacío, mostrar $0.00 inmediatamente
        if (carrito.isEmpty()) {
            lblTotal.setText("$0.00");
            return;
        }

        // Parsear el total actual
        String totalActualStr = lblTotal.getText().replace("$", "").replace(",", "");
        double totalActual = 0;
        try {
            totalActual = Double.parseDouble(totalActualStr);
        } catch (NumberFormatException e) {
            totalActual = 0;
        }

        // Animar el contador del total
        Timeline contador = new Timeline();
        for (int i = 0; i <= 20; i++) {
            double valor = totalActual + ((nuevoTotal - totalActual) * (i / 20.0));
            KeyFrame frame = new KeyFrame(
                    Duration.millis(i * 40),
                    e -> lblTotal.setText(String.format("$%,.2f", valor))
            );
            contador.getKeyFrames().add(frame);
        }

        // Efecto especial al finalizar
        contador.setOnFinished(e -> {
            ScaleTransition pop = new ScaleTransition(Duration.millis(200), lblTotal);
            pop.setFromX(1.0);
            pop.setFromY(1.0);
            pop.setToX(1.1);
            pop.setToY(1.1);
            pop.setCycleCount(2);
            pop.setAutoReverse(true);

            DropShadow goldGlow = new DropShadow();
            goldGlow.setColor(Color.web("#FFD700", 0.8));
            goldGlow.setRadius(15);
            lblTotal.setEffect(goldGlow);

            pop.setOnFinished(ev -> {
                Timeline removeGlow = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                    if (lblTotal != null) {
                        lblTotal.setEffect(null);
                    }
                }));
                removeGlow.play();
            });

            pop.play();
        });

        contador.play();
    }


    @FXML
    private void finalizarVenta() {
        if (carrito.isEmpty()) {
            animarCarritoVacio();
            NotificationUtil.mostrarError("Error", "El carrito está vacío. No se puede registrar la venta.");
            return;
        }

        double totalVenta = calcularTotal();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PaymentDialogView.fxml"));
            Parent root = loader.load();

            PaymentDialogController controller = loader.getController();

            Consumer<PagoCompleto> onPagoCompletado = pago -> {
                procesarVentaCompleta(pago);
            };

            controller.inicializar(totalVenta, onPagoCompletado, this::cancelarVenta);

            Stage stage = new Stage();
            stage.setTitle("Procesar Pago");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));

            // --- LÍNEAS AÑADIDAS PARA SOLUCIONAR EL PROBLEMA ---
            stage.setMinWidth(800);  // Establece el ancho mínimo
            stage.setMinHeight(800); // Establece el alto mínimo
            // --- FIN DE LÍNEAS AÑADIDAS ---

            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            NotificationUtil.mostrarError("Error de Sistema", "No se pudo abrir el diálogo de pago.");
        }
    }
    private void procesarVentaCompleta(PagoCompleto pago) {
        Venta nuevaVenta = new Venta();
        nuevaVenta.setDetalles(new ArrayList<>(carrito));
        nuevaVenta.setTotal(pago.getTotalVenta());
        // --- CAMBIO 3: Asignar el ID del local y del usuario a la venta ---
        nuevaVenta.setIdLocal(SessionManager.getInstance().getLocalId());
        // En el futuro, también el ID de usuario:
        // nuevaVenta.setIdUsuario(SessionManager.getInstance().getUsuarioActual().getId());
        boolean exito = ventaDAO.registrarVentaCompleta(nuevaVenta, pago);

        if (exito) {
            animarVentaExitosa();
            NotificationUtil.mostrarInformacion("Venta Exitosa", "La venta y los pagos se han registrado correctamente.");
            mostrarResumenVentaSimple(nuevaVenta); // Opcional: Mostrar resumen
            limpiarCarritoCompleto();
        } else {
            animarVentaFallida();
            NotificationUtil.mostrarError("Error de Base de Datos", "La transacción fue revertida. La venta no se guardó.");
        }
    }

    /**
     * Diálogo simplificado solo para efectivo
     */
    private void mostrarDialogoEfectivoSimple(double totalVenta) {
        Alert pagoDialog = new Alert(Alert.AlertType.CONFIRMATION);
        pagoDialog.setTitle("Procesar Pago");
        pagoDialog.setHeaderText("Pago en Efectivo");
        pagoDialog.setContentText(String.format("Total a cobrar: $%.2f\n\n¿Confirma el pago en efectivo?", totalVenta));

        ButtonType btnConfirmar = new ButtonType("Confirmar Pago");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        pagoDialog.getButtonTypes().setAll(btnConfirmar, btnCancelar);

        pagoDialog.showAndWait().ifPresent(response -> {
            if (response == btnConfirmar) {
                procesarVentaEfectivo(totalVenta);
            }
        });
    }

    /**
     * Procesa la venta con pago en efectivo
     */
    private void procesarVentaEfectivo(double totalVenta) {
        // Animación de procesamiento
        animarProcesandoVenta();

        // Usar Platform.runLater para evitar el conflicto con animaciones
        Timeline procesamiento = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            // Ejecutar el procesamiento fuera del contexto de animación
            javafx.application.Platform.runLater(() -> {
                Venta nuevaVenta = new Venta();
                nuevaVenta.setDetalles(new ArrayList<>(carrito));
                nuevaVenta.setTotal(totalVenta);

                boolean exito = ventaDAO.registrarVenta(nuevaVenta);
                if (exito) {
                    // Animación de éxito
                    animarVentaExitosa();
                    NotificationUtil.mostrarInformacion("Venta Exitosa", "La venta se ha registrado correctamente.");

                    // Mostrar resumen simple SIN conflicto
                    mostrarResumenVentaSimple(nuevaVenta);

                    // Limpiar carrito después de mostrar el resumen
                    limpiarCarritoCompleto();
                } else {
                    animarVentaFallida();
                    NotificationUtil.mostrarError("Error de Venta", "Ocurrió un error y la venta no pudo ser registrada.");
                }
            });
        }));
        procesamiento.play();
    }

    /**
     * Muestra un resumen simple de la venta
     */
    private void mostrarResumenVentaSimple(Venta venta) {
        Alert resumen = new Alert(Alert.AlertType.INFORMATION);
        resumen.setTitle("Venta Completada");
        resumen.setHeaderText("Resumen de la Transacción");
        String contenido = "Venta ID: " + venta.getId() + "\n" +
                "Fecha: " + venta.getFechaVenta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n" +
                "Total: $" + String.format("%.2f", venta.getTotal());
        resumen.setContentText(contenido);
        resumen.showAndWait();
    }

    /**
     * Animación de carrito vacío
     */
    private void animarCarritoVacio() {
        if (tablaVenta == null) return;

        TranslateTransition shake = new TranslateTransition(Duration.millis(50), tablaVenta);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);

        DropShadow redGlow = new DropShadow();
        redGlow.setColor(Color.web("#dc3545", 0.5));
        redGlow.setRadius(20);
        tablaVenta.setEffect(redGlow);

        shake.setOnFinished(e -> {
            Timeline removeEffect = new Timeline(new KeyFrame(Duration.seconds(2), ev -> {
                tablaVenta.setEffect(null);
            }));
            removeEffect.play();
        });

        shake.play();
    }

    /**
     * Animación de procesamiento
     */
    private void animarProcesandoVenta() {
        if (lblTotal == null) return;

        // Pulso en el total
        ScaleTransition pulsoTotal = new ScaleTransition(Duration.millis(600), lblTotal);
        pulsoTotal.setFromX(1.0);
        pulsoTotal.setFromY(1.0);
        pulsoTotal.setToX(1.05);
        pulsoTotal.setToY(1.05);
        pulsoTotal.setCycleCount(2);
        pulsoTotal.setAutoReverse(true);
        pulsoTotal.play();
    }

    /**
     * Animación de venta exitosa
     */
    private void animarVentaExitosa() {
        if (lblTotal == null) return;

        // Efecto dorado en el total
        DropShadow goldGlow = new DropShadow();
        goldGlow.setColor(Color.web("#FFD700", 0.9));
        goldGlow.setRadius(25);
        lblTotal.setEffect(goldGlow);

        ScaleTransition totalBounce = new ScaleTransition(Duration.millis(300), lblTotal);
        totalBounce.setFromX(1.0);
        totalBounce.setFromY(1.0);
        totalBounce.setToX(1.2);
        totalBounce.setToY(1.2);
        totalBounce.setCycleCount(2);
        totalBounce.setAutoReverse(true);
        totalBounce.setOnFinished(e -> lblTotal.setEffect(null));
        totalBounce.play();
    }

    /**
     * Animación de venta fallida
     */
    private void animarVentaFallida() {
        if (lblTotal == null) return;

        TranslateTransition shake = new TranslateTransition(Duration.millis(50), lblTotal);
        shake.setFromX(0);
        shake.setByX(8);
        shake.setCycleCount(8);
        shake.setAutoReverse(true);

        DropShadow redGlow = new DropShadow();
        redGlow.setColor(Color.web("#dc3545", 0.8));
        redGlow.setRadius(20);
        lblTotal.setEffect(redGlow);

        shake.setOnFinished(e -> {
            Timeline removeEffect = new Timeline(new KeyFrame(Duration.seconds(2), ev -> {
                lblTotal.setEffect(null);
            }));
            removeEffect.play();
        });

        shake.play();
    }

    @FXML
    private void cancelarVenta() {
        if (!carrito.isEmpty()) {
            animarLimpiezaCarrito();
            Timeline limpiar = new Timeline(new KeyFrame(Duration.millis(500), e -> limpiarCarritoCompleto()));
            limpiar.play();
        } else {
            limpiarCamposProducto();
            lblTotal.setText("$0.00");
        }
    }

    /**
     * Animación de limpieza del carrito
     */
    private void animarLimpiezaCarrito() {
        if (tablaVenta == null) return;

        FadeTransition fadeTable = new FadeTransition(Duration.millis(400), tablaVenta);
        fadeTable.setFromValue(1.0);
        fadeTable.setToValue(0.3);

        fadeTable.setOnFinished(e -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), tablaVenta);
            fadeIn.setFromValue(0.3);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeTable.play();
    }

    private void agregarBotonEliminar() {
        Callback<TableColumn<DetalleVenta, Void>, TableCell<DetalleVenta, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btn = new Button("🗑️");
            {
                btn.setStyle("-fx-background-color: #e76f51; -fx-text-fill: white; -fx-background-radius: 4px;");
                btn.setOnAction(event -> {
                    DetalleVenta detalle = getTableView().getItems().get(getIndex());
                    carrito.remove(detalle);
                    actualizarTotalConAnimacion();
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

    /**
     * Animación para eliminar un item del carrito
     */
    private void animarEliminacionItem(DetalleVenta detalle) {
        carrito.remove(detalle);
        actualizarTotalConAnimacion();
    }

    private double calcularTotal() {
        return carrito.stream().mapToDouble(DetalleVenta::getSubtotal).sum();
    }
    private void limpiarCarritoCompleto() {
        carrito.clear();
        lblTotal.setText("$0.00");
        limpiarCamposProducto();
    }

    private void limpiarCamposProducto() {
        productoSeleccionado = null;
        txtCodigoBarras.clear();
        txtNombreProducto.clear();
        spinnerCantidad.getValueFactory().setValue(1);
        txtCodigoBarras.requestFocus();
    }
}