package com.tuempresa.pos.model;

import java.time.LocalDateTime;

public class DetallePago {
    private int id;
    private int idVenta;
    private TipoPago tipo;
    private double monto;
    private String referencia; // Número de transacción, voucher, etc.
    private String autorizacion; // Código de autorización para tarjetas
    private LocalDateTime fechaPago;
    private String banco; // Para transferencias y tarjetas
    private String ultimosCuatroDigitos; // Para tarjetas

    // Constructor
    public DetallePago() {
        this.fechaPago = LocalDateTime.now();
    }

    public DetallePago(TipoPago tipo, double monto) {
        this();
        this.tipo = tipo;
        this.monto = monto;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }

    public TipoPago getTipo() { return tipo; }
    public void setTipo(TipoPago tipo) { this.tipo = tipo; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public String getAutorizacion() { return autorizacion; }
    public void setAutorizacion(String autorizacion) { this.autorizacion = autorizacion; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }

    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }

    public String getUltimosCuatroDigitos() { return ultimosCuatroDigitos; }
    public void setUltimosCuatroDigitos(String ultimosCuatroDigitos) { this.ultimosCuatroDigitos = ultimosCuatroDigitos; }

    public String getDescripcionCompleta() {
        StringBuilder desc = new StringBuilder();
        desc.append(tipo.getNombre()).append(" - $").append(String.format("%.2f", monto));

        if (referencia != null && !referencia.isEmpty()) {
            desc.append(" (Ref: ").append(referencia).append(")");
        }

        if (ultimosCuatroDigitos != null && !ultimosCuatroDigitos.isEmpty()) {
            desc.append(" ****").append(ultimosCuatroDigitos);
        }

        return desc.toString();
    }
}