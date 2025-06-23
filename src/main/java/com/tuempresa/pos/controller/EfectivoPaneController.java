package com.tuempresa.pos.controller;

import com.tuempresa.pos.model.DetallePago;
import com.tuempresa.pos.model.TipoPago;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import java.util.Optional;

public class EfectivoPaneController implements PaymentPaneController {

    @FXML private TextField txtMontoRecibido;

    @Override
    public boolean validarCampos() {
        // Para efectivo, no hay campos obligatorios adicionales
        return true;
    }

    @Override
    public Optional<DetallePago> getDetallePago(TipoPago tipo, double monto) {
        DetallePago detalle = new DetallePago(tipo, monto);
        detalle.setReferencia("EFECTIVO-" + System.currentTimeMillis());
        return Optional.of(detalle);
    }

    @Override
    public void requestFocus() {
        txtMontoRecibido.requestFocus();
    }
}