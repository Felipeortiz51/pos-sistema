package com.tuempresa.pos.service;

import com.tuempresa.pos.model.*;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Servicio para procesar diferentes tipos de pagos
 */
public class PaymentService {

    private Random random = new Random();

    /**
     * Procesa un pago según su tipo
     */
    public ResultadoPago procesarPago(TipoPago tipo, double monto, String... parametros) {
        switch (tipo) {
            case EFECTIVO:
                return procesarPagoEfectivo(monto);
            case TARJETA_CREDITO:
            case TARJETA_DEBITO:
                return procesarPagoTarjeta(tipo, monto, parametros);
            case TRANSFERENCIA:
                return procesarTransferencia(monto, parametros);
            default:
                return ResultadoPago.error("Tipo de pago no soportado", "TIPO_INVALIDO");
        }
    }

    /**
     * Procesa pago en efectivo
     */
    private ResultadoPago procesarPagoEfectivo(double monto) {
        if (monto <= 0) {
            return ResultadoPago.error("El monto debe ser mayor a cero", "MONTO_INVALIDO");
        }

        DetallePago detalle = new DetallePago(TipoPago.EFECTIVO, monto);
        detalle.setReferencia("EFECTIVO-" + System.currentTimeMillis());

        return ResultadoPago.exito("Pago en efectivo procesado correctamente", detalle);
    }

    /**
     * Procesa pago con tarjeta (simulación)
     */
    private ResultadoPago procesarPagoTarjeta(TipoPago tipo, double monto, String... parametros) {
        if (monto <= 0) {
            return ResultadoPago.error("El monto debe ser mayor a cero", "MONTO_INVALIDO");
        }

        // Simular procesamiento de tarjeta
        try {
            Thread.sleep(1000); // Simular delay de procesamiento
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simular posible fallo (5% de probabilidad)
        if (random.nextInt(100) < 5) {
            return ResultadoPago.error("Transacción rechazada por el banco", "TARJETA_RECHAZADA");
        }

        DetallePago detalle = new DetallePago(tipo, monto);

        // Generar datos simulados
        String numeroTarjeta = parametros.length > 0 ? parametros[0] : generarNumeroTarjeta();
        detalle.setUltimosCuatroDigitos(numeroTarjeta.substring(numeroTarjeta.length() - 4));
        detalle.setAutorizacion(generarCodigoAutorizacion());
        detalle.setReferencia("TXN-" + System.currentTimeMillis());
        detalle.setBanco(parametros.length > 1 ? parametros[1] : "BANCO EJEMPLO");

        return ResultadoPago.exito("Transacción aprobada", detalle);
    }

    /**
     * Procesa transferencia bancaria
     */
    private ResultadoPago procesarTransferencia(double monto, String... parametros) {
        if (monto <= 0) {
            return ResultadoPago.error("El monto debe ser mayor a cero", "MONTO_INVALIDO");
        }

        String numeroReferencia = parametros.length > 0 ? parametros[0] : "";
        if (numeroReferencia.isEmpty()) {
            return ResultadoPago.error("Número de referencia requerido", "REFERENCIA_REQUERIDA");
        }

        DetallePago detalle = new DetallePago(TipoPago.TRANSFERENCIA, monto);
        detalle.setReferencia(numeroReferencia);
        detalle.setBanco(parametros.length > 1 ? parametros[1] : "BANCO RECEPTOR");

        return ResultadoPago.exito("Transferencia registrada correctamente", detalle);
    }

    /**
     * Calcula el cambio para pagos en efectivo
     */
    public double calcularCambio(double totalVenta, double montoRecibido) {
        return Math.max(0, montoRecibido - totalVenta);
    }

    /**
     * Valida si un monto es válido para el tipo de pago
     */
    public boolean validarMonto(TipoPago tipo, double monto, double totalVenta) {
        if (monto <= 0) return false;

        switch (tipo) {
            case EFECTIVO:
                return true; // El efectivo puede ser mayor al total (para dar cambio)
            case TARJETA_CREDITO:
            case TARJETA_DEBITO:
            case TRANSFERENCIA:
                return monto <= totalVenta; // No puede ser mayor al total
            default:
                return false;
        }
    }

    /**
     * Genera un número de tarjeta simulado para testing
     */
    private String generarNumeroTarjeta() {
        return "4" + String.format("%015d", random.nextLong() % 1000000000000000L);
    }

    /**
     * Genera un código de autorización simulado
     */
    private String generarCodigoAutorizacion() {
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * Obtiene las comisiones por tipo de pago (para futuras implementaciones)
     */
    public double obtenerComision(TipoPago tipo, double monto) {
        switch (tipo) {
            case TARJETA_CREDITO:
                return monto * 0.03; // 3% de comisión
            case TARJETA_DEBITO:
                return monto * 0.015; // 1.5% de comisión
            case TRANSFERENCIA:
                return 2.0; // Comisión fija
            case EFECTIVO:
            default:
                return 0.0; // Sin comisión
        }
    }

    /**
     * Verifica si un tipo de pago está disponible
     */
    public boolean isMetodoPagoDisponible(TipoPago tipo) {
        switch (tipo) {
            case EFECTIVO:
                return true; // Siempre disponible
            case TARJETA_CREDITO:
            case TARJETA_DEBITO:
                return verificarConexionTPV(); // Verificar conexión con terminal
            case TRANSFERENCIA:
                return verificarSistemaBancario(); // Verificar sistema bancario
            default:
                return false;
        }
    }

    /**
     * Simula verificación de conexión con TPV
     */
    private boolean verificarConexionTPV() {
        // En una implementación real, verificarías la conexión con el terminal de pagos
        return random.nextInt(100) < 95; // 95% de disponibilidad simulada
    }

    /**
     * Simula verificación del sistema bancario
     */
    private boolean verificarSistemaBancario() {
        // En una implementación real, verificarías la conexión con el sistema bancario
        return random.nextInt(100) < 90; // 90% de disponibilidad simulada
    }

    /**
     * Obtiene información del método de pago para mostrar al usuario
     */
    public String obtenerInfoMetodoPago(TipoPago tipo) {
        switch (tipo) {
            case EFECTIVO:
                return "Pago inmediato • Sin comisiones • Permite cambio";
            case TARJETA_CREDITO:
                return "Procesamiento 1-2 seg • Comisión 3% • Requiere autorización";
            case TARJETA_DEBITO:
                return "Procesamiento 1-2 seg • Comisión 1.5% • Débito inmediato";
            case TRANSFERENCIA:
                return "Requiere referencia • Comisión $2.00 • Verificación manual";
            default:
                return "Método de pago no disponible";
        }
    }
}