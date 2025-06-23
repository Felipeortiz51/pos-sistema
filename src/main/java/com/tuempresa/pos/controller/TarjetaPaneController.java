package com.tuempresa.pos.controller;

import com.tuempresa.pos.model.DetallePago;
import com.tuempresa.pos.model.TipoPago;
import com.tuempresa.pos.util.NotificationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import java.util.Optional;

public class TarjetaPaneController implements PaymentPaneController {

    @FXML private TextField txtNumeroTarjeta;
    @FXML private TextField txtCodigoSeguridad;
    @FXML private TextField txtBanco;

    @Override
    public boolean validarCampos() {
        if (txtNumeroTarjeta.getText().trim().isEmpty() || txtNumeroTarjeta.getText().length() < 13) {
            NotificationUtil.mostrarAdvertencia("Campo requerido", "Ingrese un número de tarjeta válido.");
            return false;
        }
        if (txtCodigoSeguridad.getText().trim().isEmpty() || txtCodigoSeguridad.getText().length() < 3) {
            NotificationUtil.mostrarAdvertencia("Campo requerido", "Ingrese el código de seguridad (CVV).");
            return false;
        }
        return true;
    }

    @Override
    public Optional<DetallePago> getDetallePago(TipoPago tipo, double monto) {
        String numeroTarjeta = txtNumeroTarjeta.getText().trim();
        String ultimosDigitos = numeroTarjeta.substring(numeroTarjeta.length() - 4);

        DetallePago detalle = new DetallePago(tipo, monto);
        detalle.setBanco(txtBanco.getText());
        detalle.setUltimosCuatroDigitos(ultimosDigitos);
        // En una implementación real, la autorización y referencia vendrían de una pasarela de pago
        detalle.setAutorizacion(String.valueOf((int)(Math.random() * 900000) + 100000));
        detalle.setReferencia("TXN-" + System.currentTimeMillis());

        return Optional.of(detalle);
    }

    @Override
    public void requestFocus() {
        txtNumeroTarjeta.requestFocus();
    }
}