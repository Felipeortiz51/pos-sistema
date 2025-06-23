package com.tuempresa.pos.controller;

import com.tuempresa.pos.model.*;
import com.tuempresa.pos.service.PaymentService;
import com.tuempresa.pos.util.NotificationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public class PaymentDialogController {

    // --- DECLARACIONES @FXML REQUERIDAS POR TU FXML ---
    @FXML private Label lblTotalVenta;
    @FXML private Label lblSubtitulo; // <-- Necesitas esta línea
    @FXML private Label lblTotalPagado;
    @FXML private Label lblFaltante;
    @FXML private Label lblCambio;
    @FXML private VBox vboxFormularioPago;
    @FXML private HBox hboxCambio;
    @FXML private Label lblTipoSeleccionado;
    @FXML private TextField txtMonto;
    @FXML private VBox vboxCamposEspecificos;
    @FXML private ListView<DetallePago> listPagos;
    @FXML private Button btnCompletarVenta;
    @FXML private Button btnCancelarPago; // <-- Necesitas esta línea
    @FXML private Button btnProcesarPago; // <-- Necesitas esta línea
    @FXML private Button btnCancelar; // El botón de "Cancelar Venta"
    // --- FIN DE DECLARACIONES @FXML ---

    private PaymentService paymentService;
    private PagoCompleto pagoCompleto;
    private TipoPago tipoSeleccionado;
    private PaymentPaneController panelControllerActual;
    private Consumer<PagoCompleto> onPagoCompletado;
    private Runnable onCancelado;

    @FXML
    public void initialize() {
        paymentService = new PaymentService();
        configurarCampoMonto();
        configurarListaPagos();
    }

    public void inicializar(double totalVenta, Consumer<PagoCompleto> onCompletado, Runnable onCancelado) {
        this.pagoCompleto = new PagoCompleto(totalVenta);
        this.onPagoCompletado = onCompletado;
        this.onCancelado = onCancelado;
        lblTotalVenta.setText(String.format("$%,.2f", totalVenta));
        actualizarResumen();
        actualizarEstadoBotones();
        seleccionarEfectivo();
    }

    // --- MÉTODOS INVOCADOS POR onAction DESDE EL FXML ---

    @FXML
    private void seleccionarEfectivo() {
        seleccionarTipoPago(TipoPago.EFECTIVO, "EfectivoPane.fxml");
    }

    @FXML
    private void seleccionarTarjetaCredito() {
        seleccionarTipoPago(TipoPago.TARJETA_CREDITO, "TarjetaPane.fxml");
    }

    @FXML
    private void seleccionarTarjetaDebito() {
        seleccionarTipoPago(TipoPago.TARJETA_DEBITO, "TarjetaPane.fxml");
    }

    @FXML
    private void seleccionarTransferencia() {
        seleccionarTipoPago(TipoPago.TRANSFERENCIA, "TransferenciaPane.fxml");
    }

    @FXML
    private void seleccionarPagoMixto() {
        tipoSeleccionado = TipoPago.MIXTO;
        lblTipoSeleccionado.setText("🔄 Pago Mixto");
        lblSubtitulo.setText("Añada pagos parciales con distintos métodos");
        vboxCamposEspecificos.getChildren().clear();
        panelControllerActual = null;
        NotificationUtil.mostrarInformacion("Modo Pago Mixto", "Seleccione un método para añadir el primer pago parcial.");
    }

    @FXML
    private void cancelarPago() {
        limpiarFormularioDeEntrada();
        NotificationUtil.mostrarInformacion("Cancelado", "Se ha limpiado el formulario de pago actual.");
    }

    @FXML
    private void procesarPago() {
        if (tipoSeleccionado == null || tipoSeleccionado == TipoPago.MIXTO || panelControllerActual == null) {
            NotificationUtil.mostrarAdvertencia("Selección requerida", "Por favor, seleccione un método de pago específico (Efectivo, Tarjeta, etc.).");
            return;
        }
        String montoTexto = txtMonto.getText().replace(",", ".");
        if (montoTexto.isEmpty() || Double.parseDouble(montoTexto) <= 0) {
            NotificationUtil.mostrarAdvertencia("Monto inválido", "El monto a pagar debe ser mayor a cero.");
            return;
        }
        if (!panelControllerActual.validarCampos()) {
            return;
        }
        double monto = Double.parseDouble(montoTexto);
        Optional<DetallePago> detalleOpt = panelControllerActual.getDetallePago(tipoSeleccionado, monto);

        if (detalleOpt.isPresent()) {
            pagoCompleto.agregarPago(detalleOpt.get());
            actualizarListaPagos();
            actualizarResumen();
            actualizarEstadoBotones();
            limpiarFormularioDeEntrada();
        } else {
            NotificationUtil.mostrarError("Error en Pago", "No se pudieron obtener los detalles del pago.");
        }
    }

    @FXML
    private void completarVenta() {
        if (pagoCompleto.isPagoCompleto() && onPagoCompletado != null) {
            onPagoCompletado.accept(pagoCompleto);
            cerrarVentana();
        }
    }

    @FXML
    private void cancelarVenta() {
        if (onCancelado != null) {
            onCancelado.run();
        }
        cerrarVentana();
    }

    // --- MÉTODOS INTERNOS DE AYUDA ---

    private void seleccionarTipoPago(TipoPago tipo, String fxmlPath) {
        this.tipoSeleccionado = tipo;
        lblTipoSeleccionado.setText(tipo.getIcono() + " " + tipo.getNombre());
        lblSubtitulo.setText("Completar la información para " + tipo.getNombre());
        txtMonto.clear();
        cargarPanelPago(fxmlPath);
        txtMonto.requestFocus();
    }

    private void cargarPanelPago(String fxmlPath) {
        try {
            vboxCamposEspecificos.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/panes/" + fxmlPath));
            Node panelNode = loader.load();
            panelControllerActual = loader.getController();
            vboxCamposEspecificos.getChildren().add(panelNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configurarListaPagos() {
        listPagos.setCellFactory(param -> new ListCell<>() {
            private final HBox hbox = new HBox(10);
            private final Label lblDescripcion = new Label();
            private final Button btnEliminar = new Button("❌");
            {
                hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                HBox.setHgrow(lblDescripcion, javafx.scene.layout.Priority.ALWAYS);
                btnEliminar.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc3545; -fx-cursor: hand; -fx-font-size: 14px;");
                btnEliminar.setOnAction(event -> {
                    DetallePago pago = getItem();
                    if (pago != null) {
                        pagoCompleto.eliminarPago(pago);
                        actualizarListaPagos();
                        actualizarResumen();
                        actualizarEstadoBotones();
                    }
                });
                hbox.getChildren().addAll(lblDescripcion, btnEliminar);
            }
            @Override
            protected void updateItem(DetallePago item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    lblDescripcion.setText(item.getDescripcionCompleta());
                    setGraphic(hbox);
                }
            }
        });
    }

    private void configurarCampoMonto() {
        txtMonto.focusedProperty().addListener((obs, oldV, newV) -> {
            if (newV && txtMonto.getText().isEmpty() && pagoCompleto != null) {
                double faltante = pagoCompleto.getFaltante();
                if (faltante > 0) {
                    txtMonto.setText(String.format("%.2f", faltante).replace(",", "."));
                }
            }
        });
    }

    private void actualizarListaPagos() {
        listPagos.getItems().setAll(pagoCompleto.getDetallesPago());
    }

    private void actualizarResumen() {
        lblTotalPagado.setText(String.format("$%,.2f", pagoCompleto.getTotalPagado()));
        lblFaltante.setText(String.format("$%,.2f", pagoCompleto.getFaltante()));
        lblCambio.setText(String.format("$%,.2f", pagoCompleto.getCambio()));
        hboxCambio.setVisible(pagoCompleto.getCambio() > 0);
    }

    private void actualizarEstadoBotones() {
        btnCompletarVenta.setDisable(!pagoCompleto.isPagoCompleto());
    }

    private void limpiarFormularioDeEntrada() {
        txtMonto.clear();
        if(panelControllerActual != null) {
            panelControllerActual.requestFocus();
        }
    }

    private void cerrarVentana() {
        Stage stage = (Stage) lblTotalVenta.getScene().getWindow();
        stage.close();
    }
}