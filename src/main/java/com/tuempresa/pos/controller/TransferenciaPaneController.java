package com.tuempresa.pos.controller;

import com.tuempresa.pos.model.DetallePago;
import com.tuempresa.pos.model.TipoPago;
import com.tuempresa.pos.util.NotificationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import java.util.Optional;

public class TransferenciaPaneController implements PaymentPaneController {

    @FXML private TextField txtReferencia;
    @FXML private TextField txtBanco;

    @Override
    public boolean validarCampos() {
        if (txtReferencia.getText().trim().isEmpty()) {
            NotificationUtil.mostrarAdvertencia("Campo requerido", "Ingrese el número de referencia de la transferencia.");
            return false;
        }
        return true;
    }

    @Override
    public Optional<DetallePago> getDetallePago(TipoPago tipo, double monto) {
        DetallePago detalle = new DetallePago(tipo, monto);
        detalle.setReferencia(txtReferencia.getText().trim());
        detalle.setBanco(txtBanco.getText().trim());
        return Optional.of(detalle);
    }

    @Override
    public void requestFocus() {
        txtReferencia.requestFocus();
    }
}