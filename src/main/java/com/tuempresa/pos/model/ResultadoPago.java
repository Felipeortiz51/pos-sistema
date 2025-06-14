package com.tuempresa.pos.model;

public class ResultadoPago {
    private boolean exitoso;
    private String mensaje;
    private String codigoError;
    private DetallePago detallePago;

    public ResultadoPago(boolean exitoso, String mensaje) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
    }

    public static ResultadoPago exito(String mensaje, DetallePago detalle) {
        ResultadoPago resultado = new ResultadoPago(true, mensaje);
        resultado.setDetallePago(detalle);
        return resultado;
    }

    public static ResultadoPago error(String mensaje, String codigoError) {
        ResultadoPago resultado = new ResultadoPago(false, mensaje);
        resultado.setCodigoError(codigoError);
        return resultado;
    }

    // Getters y Setters
    public boolean isExitoso() { return exitoso; }
    public void setExitoso(boolean exitoso) { this.exitoso = exitoso; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getCodigoError() { return codigoError; }
    public void setCodigoError(String codigoError) { this.codigoError = codigoError; }

    public DetallePago getDetallePago() { return detallePago; }
    public void setDetallePago(DetallePago detallePago) { this.detallePago = detallePago; }
}