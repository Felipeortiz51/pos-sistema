package com.tuempresa.pos.controller;

import com.tuempresa.pos.model.DetallePago;
import com.tuempresa.pos.model.TipoPago;
import java.util.Optional;

/**
 * Interfaz para todos los controladores de paneles de pago.
 * Garantiza que cada panel pueda ser validado y pueda proporcionar
 * los detalles del pago que gestiona.
 */
public interface PaymentPaneController {

    /**
     * Valida los campos de entrada específicos del panel.
     * @return true si los datos son válidos, false en caso contrario.
     */
    boolean validarCampos();

    /**
     * Crea y devuelve un objeto DetallePago con la información del panel.
     *
     * @param tipo El tipo de pago que se está procesando.
     * @param monto El monto del pago.
     * @return Un Optional que contiene el DetallePago si la creación fue exitosa.
     */
    Optional<DetallePago> getDetallePago(TipoPago tipo, double monto);

    /**
     * Pone el foco en el campo de entrada principal del panel.
     */
    void requestFocus();
}