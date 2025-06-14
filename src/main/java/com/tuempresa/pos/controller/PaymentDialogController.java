package com.tuempresa.pos.controller;

import com.tuempresa.pos.model.*;
import com.tuempresa.pos.service.PaymentService;
import com.tuempresa.pos.util.NotificationUtil;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.function.Consumer;

public class PaymentDialogController {

    @FXML private Label lblTotalVenta;
    @FXML private Label lblTotalPagado;
    @FXML private Label lblFaltante;
    @FXML private Label lblCambio;
    @FXML private HBox hboxCambio;
    @FXML private Label lblTipoSeleccionado;
    @FXML private TextField txtMonto;
    @FXML private VBox vboxCamposEspecificos;
    @FXML private ListView<String> listPagos;
    @FXML private Button btnCompletarVenta;
    @FXML private VBox vboxFormularioPago;

    // Botones de métodos de pago
    @FXML private Button btnEfectivo;
    @FXML private Button btnTarjetaCredito;
    @FXML private Button btnTarjetaDebito;
    @FXML private Button btnTransferencia;
    @FXML private Button btnPagoMixto;

    private PaymentService paymentService;
    private PagoCompleto pagoCompleto;
    private TipoPago tipoSeleccionado;
    private Consumer<PagoCompleto> onPagoCompletado;
    private Runnable onCancelado;

    // Campos específicos para cada tipo de pago
    private TextField txtNumeroTarjeta;
    private TextField txtCodigoSeguridad;
    private TextField txtBanco;
    private TextField txtReferencia;
    private TextField txtMontoRecibido;

    @FXML
    public void initialize() {
        paymentService = new PaymentService();
        setupAnimaciones();
        configurarCampoMonto();
        // Quitar esta línea que causaba error
        // actualizarEstadoBotones();
    }

    public void inicializar(double totalVenta, Consumer<PagoCompleto> onCompletado, Runnable onCancelado) {
        this.pagoCompleto = new PagoCompleto(totalVenta);
        this.onPagoCompletado = onCompletado;
        this.onCancelado = onCancelado;

        lblTotalVenta.setText(String.format("$%,.2f", totalVenta));
        actualizarResumen();
        actualizarEstadoBotones(); // Mover aquí para evitar NullPointerException
    }

    private void setupAnimaciones() {
        // Configurar animaciones hover para botones de métodos de pago
        Button[] botones = {btnEfectivo, btnTarjetaCredito, btnTarjetaDebito, btnTransferencia, btnPagoMixto};

        for (Button boton : botones) {
            if (boton != null) { // Verificar que el botón no sea null
                configurarAnimacionBoton(boton);
            }
        }
    }

    private void configurarAnimacionBoton(Button boton) {
        boton.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), boton);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();

            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web("#000000", 0.3));
            shadow.setRadius(10);
            shadow.setOffsetY(3);
            boton.setEffect(shadow);
        });

        boton.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), boton);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();

            boton.setEffect(null);
        });
    }

    private void configurarCampoMonto() {
        // Formatear automáticamente el campo de monto
        txtMonto.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*\\.?\\d*")) {
                txtMonto.setText(oldText);
            }
        });

        // Sugerir monto faltante
        txtMonto.focusedProperty().addListener((obs, oldFocused, newFocused) -> {
            if (newFocused && txtMonto.getText().isEmpty() && pagoCompleto != null) {
                double faltante = pagoCompleto.getFaltante();
                if (faltante > 0) {
                    txtMonto.setText(String.format("%.2f", faltante));
                }
            }
        });
    }

    @FXML
    private void seleccionarEfectivo() {
        seleccionarTipoPago(TipoPago.EFECTIVO);
        animarSeleccionBoton(btnEfectivo);
        mostrarCamposEfectivo();
    }

    @FXML
    private void seleccionarTarjetaCredito() {
        if (!paymentService.isMetodoPagoDisponible(TipoPago.TARJETA_CREDITO)) {
            mostrarErrorDisponibilidad("Terminal de tarjetas no disponible");
            return;
        }
        seleccionarTipoPago(TipoPago.TARJETA_CREDITO);
        animarSeleccionBoton(btnTarjetaCredito);
        mostrarCamposTarjeta();
    }

    @FXML
    private void seleccionarTarjetaDebito() {
        if (!paymentService.isMetodoPagoDisponible(TipoPago.TARJETA_DEBITO)) {
            mostrarErrorDisponibilidad("Terminal de tarjetas no disponible");
            return;
        }
        seleccionarTipoPago(TipoPago.TARJETA_DEBITO);
        animarSeleccionBoton(btnTarjetaDebito);
        mostrarCamposTarjeta();
    }

    @FXML
    private void seleccionarTransferencia() {
        seleccionarTipoPago(TipoPago.TRANSFERENCIA);
        animarSeleccionBoton(btnTransferencia);
        mostrarCamposTransferencia();
    }

    @FXML
    private void seleccionarPagoMixto() {
        seleccionarTipoPago(TipoPago.MIXTO);
        animarSeleccionBoton(btnPagoMixto);
        mostrarInformacionPagoMixto();
    }

    private void seleccionarTipoPago(TipoPago tipo) {
        this.tipoSeleccionado = tipo;
        lblTipoSeleccionado.setText(tipo.getIcono() + " " + tipo.getNombre());

        // Limpiar campos
        txtMonto.clear();
        vboxCamposEspecificos.getChildren().clear();
    }

    private void animarSeleccionBoton(Button botonSeleccionado) {
        // Restaurar todos los botones
        Button[] botones = {btnEfectivo, btnTarjetaCredito, btnTarjetaDebito, btnTransferencia, btnPagoMixto};
        for (Button boton : botones) {
            if (boton != null) {
                boton.setOpacity(0.7);
                boton.setScaleX(1.0);
                boton.setScaleY(1.0);
            }
        }

        // Destacar botón seleccionado
        if (botonSeleccionado != null) {
            botonSeleccionado.setOpacity(1.0);

            ScaleTransition highlight = new ScaleTransition(Duration.millis(300), botonSeleccionado);
            highlight.setFromX(1.0);
            highlight.setFromY(1.0);
            highlight.setToX(1.1);
            highlight.setToY(1.1);
            highlight.setCycleCount(2);
            highlight.setAutoReverse(true);
            highlight.play();
        }
    }

    private void mostrarCamposEfectivo() {
        VBox campos = new VBox(12);

        Label lblInfo = new Label("💡 " + paymentService.obtenerInfoMetodoPago(TipoPago.EFECTIVO));
        lblInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-wrap-text: true;");

        Label lblMontoRecibido = new Label("Monto Recibido:");
        lblMontoRecibido.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");

        txtMontoRecibido = new TextField();
        txtMontoRecibido.setPromptText("Ingrese el monto recibido");
        txtMontoRecibido.setStyle("-fx-font-size: 16px; -fx-padding: 10;");

        // Calcular cambio automáticamente
        txtMontoRecibido.textProperty().addListener((obs, oldText, newText) -> {
            try {
                if (!newText.isEmpty()) {
                    double montoRecibido = Double.parseDouble(newText);
                    double cambio = paymentService.calcularCambio(pagoCompleto.getFaltante(), montoRecibido);
                    lblCambio.setText(String.format("$%,.2f", cambio));
                    hboxCambio.setVisible(cambio > 0);

                    // Actualizar campo monto
                    txtMonto.setText(String.format("%.2f", Math.min(montoRecibido, pagoCompleto.getFaltante())));
                }
            } catch (NumberFormatException e) {
                hboxCambio.setVisible(false);
            }
        });

        campos.getChildren().addAll(lblInfo, lblMontoRecibido, txtMontoRecibido);
        vboxCamposEspecificos.getChildren().add(campos);

        // Animar entrada
        animarEntradaCampos(campos);
    }

    private void mostrarCamposTarjeta() {
        VBox campos = new VBox(12);

        Label lblInfo = new Label("💡 " + paymentService.obtenerInfoMetodoPago(tipoSeleccionado));
        lblInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-wrap-text: true;");

        Label lblTarjeta = new Label("Número de Tarjeta:");
        lblTarjeta.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");

        txtNumeroTarjeta = new TextField();
        txtNumeroTarjeta.setPromptText("**** **** **** 1234");
        txtNumeroTarjeta.setStyle("-fx-font-size: 16px; -fx-padding: 10;");

        HBox hboxTarjeta = new HBox(12);

        VBox vboxCodigo = new VBox(6);
        Label lblCodigo = new Label("CVV:");
        lblCodigo.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");
        txtCodigoSeguridad = new TextField();
        txtCodigoSeguridad.setPromptText("123");
        txtCodigoSeguridad.setPrefWidth(80);
        vboxCodigo.getChildren().addAll(lblCodigo, txtCodigoSeguridad);

        VBox vboxBanco = new VBox(6);
        Label lblBancoTarjeta = new Label("Banco:");
        lblBancoTarjeta.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");
        txtBanco = new TextField();
        txtBanco.setPromptText("Nombre del banco");
        vboxBanco.getChildren().addAll(lblBancoTarjeta, txtBanco);

        hboxTarjeta.getChildren().addAll(vboxCodigo, vboxBanco);

        campos.getChildren().addAll(lblInfo, lblTarjeta, txtNumeroTarjeta, hboxTarjeta);
        vboxCamposEspecificos.getChildren().add(campos);

        animarEntradaCampos(campos);
    }

    private void mostrarCamposTransferencia() {
        VBox campos = new VBox(12);

        Label lblInfo = new Label("💡 " + paymentService.obtenerInfoMetodoPago(TipoPago.TRANSFERENCIA));
        lblInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-wrap-text: true;");

        Label lblReferencia = new Label("Número de Referencia:");
        lblReferencia.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");

        txtReferencia = new TextField();
        txtReferencia.setPromptText("Ingrese el número de referencia");
        txtReferencia.setStyle("-fx-font-size: 16px; -fx-padding: 10;");

        Label lblBancoTransfer = new Label("Banco Origen:");
        lblBancoTransfer.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");

        txtBanco = new TextField();
        txtBanco.setPromptText("Nombre del banco");
        txtBanco.setStyle("-fx-font-size: 16px; -fx-padding: 10;");

        campos.getChildren().addAll(lblInfo, lblReferencia, txtReferencia, lblBancoTransfer, txtBanco);
        vboxCamposEspecificos.getChildren().add(campos);

        animarEntradaCampos(campos);
    }

    private void mostrarInformacionPagoMixto() {
        VBox campos = new VBox(12);

        Label lblTitulo = new Label("🔄 Pago Mixto");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: 600;");

        Label lblInfo = new Label("Puede combinar diferentes métodos de pago para completar la transacción. " +
                "Seleccione cada método y procese los pagos uno por uno.");
        lblInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d; -fx-wrap-text: true;");

        Label lblInstruccion = new Label("1. Ingrese el monto parcial\n2. Seleccione otro método de pago\n3. Repita hasta completar el total");
        lblInstruccion.setStyle("-fx-font-size: 12px; -fx-text-fill: #495057; -fx-background-color: #f8f9fa; -fx-padding: 12; -fx-background-radius: 6;");

        campos.getChildren().addAll(lblTitulo, lblInfo, lblInstruccion);
        vboxCamposEspecificos.getChildren().add(campos);

        animarEntradaCampos(campos);
    }

    private void animarEntradaCampos(VBox campos) {
        campos.setOpacity(0);
        campos.setTranslateX(20);

        FadeTransition fade = new FadeTransition(Duration.millis(400), campos);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(400), campos);
        slide.setFromX(20);
        slide.setToX(0);

        ParallelTransition animation = new ParallelTransition(fade, slide);
        animation.play();
    }

    @FXML
    private void procesarPago() {
        if (tipoSeleccionado == null) {
            NotificationUtil.mostrarAdvertencia("Selección requerida", "Seleccione un método de pago");
            return;
        }

        if (!validarCampos()) {
            return;
        }

        double monto = Double.parseDouble(txtMonto.getText());

        if (!paymentService.validarMonto(tipoSeleccionado, monto, pagoCompleto.getFaltante())) {
            NotificationUtil.mostrarError("Monto inválido", "El monto ingresado no es válido para este tipo de pago");
            return;
        }

        // Animar procesamiento
        animarProcesamiento();

        // Procesar pago según el tipo
        ResultadoPago resultado = procesarSegunTipo(monto);

        if (resultado.isExitoso()) {
            pagoCompleto.agregarPago(resultado.getDetallePago());
            actualizarListaPagos();
            actualizarResumen();
            limpiarFormulario();

            // Animación de éxito
            animarPagoExitoso();

            NotificationUtil.mostrarInformacion("Pago procesado", resultado.getMensaje());
        } else {
            // Animación de error
            animarPagoFallido();

            NotificationUtil.mostrarError("Error en el pago", resultado.getMensaje());
        }

        actualizarEstadoBotones();
    }

    private boolean validarCampos() {
        if (txtMonto.getText().isEmpty()) {
            NotificationUtil.mostrarAdvertencia("Campo requerido", "Ingrese el monto a pagar");
            txtMonto.requestFocus();
            return false;
        }

        try {
            double monto = Double.parseDouble(txtMonto.getText());
            if (monto <= 0) {
                NotificationUtil.mostrarAdvertencia("Monto inválido", "El monto debe ser mayor a cero");
                return false;
            }
        } catch (NumberFormatException e) {
            NotificationUtil.mostrarAdvertencia("Formato inválido", "Ingrese un monto válido");
            return false;
        }

        // Validaciones específicas por tipo
        switch (tipoSeleccionado) {
            case TARJETA_CREDITO:
            case TARJETA_DEBITO:
                if (txtNumeroTarjeta == null || txtNumeroTarjeta.getText().isEmpty()) {
                    NotificationUtil.mostrarAdvertencia("Campo requerido", "Ingrese el número de tarjeta");
                    return false;
                }
                break;
            case TRANSFERENCIA:
                if (txtReferencia == null || txtReferencia.getText().isEmpty()) {
                    NotificationUtil.mostrarAdvertencia("Campo requerido", "Ingrese el número de referencia");
                    return false;
                }
                break;
        }

        return true;
    }

    private ResultadoPago procesarSegunTipo(double monto) {
        switch (tipoSeleccionado) {
            case EFECTIVO:
                return paymentService.procesarPago(TipoPago.EFECTIVO, monto);

            case TARJETA_CREDITO:
            case TARJETA_DEBITO:
                return paymentService.procesarPago(tipoSeleccionado, monto,
                        txtNumeroTarjeta != null ? txtNumeroTarjeta.getText() : "",
                        txtBanco != null ? txtBanco.getText() : "");

            case TRANSFERENCIA:
                return paymentService.procesarPago(TipoPago.TRANSFERENCIA, monto,
                        txtReferencia != null ? txtReferencia.getText() : "",
                        txtBanco != null ? txtBanco.getText() : "");

            default:
                return ResultadoPago.error("Tipo de pago no implementado", "TIPO_NO_IMPLEMENTADO");
        }
    }

    private void animarProcesamiento() {
        // Simular delay de procesamiento
        Timeline procesamiento = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            // El procesamiento se completa automáticamente
        }));
        procesamiento.play();
    }

    private void animarPagoExitoso() {
        // Efecto de éxito en el resumen
        DropShadow greenGlow = new DropShadow();
        greenGlow.setColor(Color.web("#28a745", 0.6));
        greenGlow.setRadius(15);
        lblTotalPagado.setEffect(greenGlow);

        ScaleTransition bounce = new ScaleTransition(Duration.millis(300), lblTotalPagado);
        bounce.setFromX(1.0);
        bounce.setFromY(1.0);
        bounce.setToX(1.1);
        bounce.setToY(1.1);
        bounce.setCycleCount(2);
        bounce.setAutoReverse(true);
        bounce.setOnFinished(e -> lblTotalPagado.setEffect(null));
        bounce.play();
    }

    private void animarPagoFallido() {
        // Efecto de error en el formulario
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), vboxFormularioPago);
        shake.setFromX(0);
        shake.setByX(5);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();

        DropShadow redGlow = new DropShadow();
        redGlow.setColor(Color.web("#dc3545", 0.6));
        redGlow.setRadius(10);
        txtMonto.setEffect(redGlow);

        Timeline removeEffect = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            txtMonto.setEffect(null);
        }));
        removeEffect.play();
    }

    private void actualizarListaPagos() {
        listPagos.getItems().clear();
        for (DetallePago pago : pagoCompleto.getDetallesPago()) {
            String item = String.format("%s %s - %s",
                    pago.getTipo().getIcono(),
                    pago.getTipo().getNombre(),
                    pago.getDescripcionCompleta());
            listPagos.getItems().add(item);
        }
    }

    private void actualizarResumen() {
        lblTotalPagado.setText(String.format("$%,.2f", pagoCompleto.getTotalPagado()));
        lblFaltante.setText(String.format("$%,.2f", pagoCompleto.getFaltante()));

        if (pagoCompleto.getCambio() > 0) {
            lblCambio.setText(String.format("$%,.2f", pagoCompleto.getCambio()));
            hboxCambio.setVisible(true);
        } else {
            hboxCambio.setVisible(false);
        }

        // Cambiar colores según el estado
        if (pagoCompleto.isPagoCompleto()) {
            lblFaltante.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #28a745;");
        } else {
            lblFaltante.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #dc3545;");
        }
    }

    private void actualizarEstadoBotones() {
        if (btnCompletarVenta != null && pagoCompleto != null) {
            btnCompletarVenta.setDisable(!pagoCompleto.isPagoCompleto());

            if (pagoCompleto.isPagoCompleto()) {
                btnCompletarVenta.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 12 24; -fx-background-radius: 6px; -fx-font-weight: 600; -fx-font-size: 16px;");

                // Animación de disponibilidad
                ScaleTransition highlight = new ScaleTransition(Duration.millis(500), btnCompletarVenta);
                highlight.setFromX(1.0);
                highlight.setFromY(1.0);
                highlight.setToX(1.05);
                highlight.setToY(1.05);
                highlight.setCycleCount(2);
                highlight.setAutoReverse(true);
                highlight.play();
            }
        }
    }

    private void limpiarFormulario() {
        txtMonto.clear();
        vboxCamposEspecificos.getChildren().clear();
        tipoSeleccionado = null;
        lblTipoSeleccionado.setText("Seleccione un método de pago");

        // Restaurar opacidad de botones
        Button[] botones = {btnEfectivo, btnTarjetaCredito, btnTarjetaDebito, btnTransferencia, btnPagoMixto};
        for (Button boton : botones) {
            if (boton != null) {
                boton.setOpacity(1.0);
            }
        }
    }

    private void mostrarErrorDisponibilidad(String mensaje) {
        NotificationUtil.mostrarError("Servicio no disponible", mensaje);

        // Efecto visual de no disponible
        TranslateTransition shake = new TranslateTransition(Duration.millis(100), lblTipoSeleccionado);
        shake.setFromX(0);
        shake.setByX(5);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }

    @FXML
    private void cancelarPago() {
        if (pagoCompleto != null && !pagoCompleto.getDetallesPago().isEmpty()) {
            // Confirmar si hay pagos procesados
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Cancelar Pago");
            confirmacion.setHeaderText("¿Está seguro de cancelar?");
            confirmacion.setContentText("Se perderán todos los pagos procesados.");

            if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                limpiarFormulario();
                pagoCompleto.getDetallesPago().clear();
                actualizarListaPagos();
                actualizarResumen();
                actualizarEstadoBotones();
            }
        } else {
            limpiarFormulario();
        }
    }

    @FXML
    private void completarVenta() {
        if (pagoCompleto == null || !pagoCompleto.isPagoCompleto()) {
            NotificationUtil.mostrarError("Pago incompleto",
                    String.format("Faltan $%.2f por pagar", pagoCompleto != null ? pagoCompleto.getFaltante() : 0));
            return;
        }

        // Animación de finalización
        animarFinalizacion(() -> {
            if (onPagoCompletado != null) {
                onPagoCompletado.accept(pagoCompleto);
            }
            cerrarVentana();
        });
    }

    @FXML
    private void cancelarVenta() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cancelar Venta");
        confirmacion.setHeaderText("¿Está seguro de cancelar la venta?");
        confirmacion.setContentText("Se perderá toda la información de la venta.");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (onCancelado != null) {
                onCancelado.run();
            }
            cerrarVentana();
        }
    }

    private void animarFinalizacion(Runnable onComplete) {
        // Efecto de éxito general
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), vboxFormularioPago.getParent());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.8);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(500), vboxFormularioPago.getParent());
        scaleDown.setFromX(1.0);
        scaleDown.setFromY(1.0);
        scaleDown.setToX(0.95);
        scaleDown.setToY(0.95);

        ParallelTransition finalAnimation = new ParallelTransition(fadeOut, scaleDown);
        finalAnimation.setOnFinished(e -> onComplete.run());
        finalAnimation.play();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) lblTotalVenta.getScene().getWindow();
        stage.close();
    }
}