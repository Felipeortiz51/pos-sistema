package com.tuempresa.pos.model;

import java.util.ArrayList;
import java.util.List;

public class PagoCompleto {
    private double totalVenta;
    private double totalPagado;
    private double cambio;
    private List<DetallePago> detallesPago;
    private boolean pagoCompleto;

    public PagoCompleto(double totalVenta) {
        this.totalVenta = totalVenta;
        this.detallesPago = new ArrayList<>();
        this.totalPagado = 0.0;
        this.cambio = 0.0;
        this.pagoCompleto = false;
    }

    public void agregarPago(DetallePago detalle) {
        detallesPago.add(detalle);
        recalcular();
    }

    public void eliminarPago(DetallePago detalle) {
        detallesPago.remove(detalle);
        recalcular();
    }

    private void recalcular() {
        totalPagado = detallesPago.stream()
                .mapToDouble(DetallePago::getMonto)
                .sum();

        cambio = Math.max(0, totalPagado - totalVenta);
        pagoCompleto = totalPagado >= totalVenta;
    }

    public double getFaltante() {
        return Math.max(0, totalVenta - totalPagado);
    }

    public boolean tienePagoEfectivo() {
        return detallesPago.stream()
                .anyMatch(pago -> pago.getTipo() == TipoPago.EFECTIVO);
    }

    public double getMontoEfectivo() {
        return detallesPago.stream()
                .filter(pago -> pago.getTipo() == TipoPago.EFECTIVO)
                .mapToDouble(DetallePago::getMonto)
                .sum();
    }

    // Getters y Setters
    public double getTotalVenta() { return totalVenta; }
    public void setTotalVenta(double totalVenta) {
        this.totalVenta = totalVenta;
        recalcular();
    }

    public double getTotalPagado() { return totalPagado; }
    public double getCambio() { return cambio; }
    public List<DetallePago> getDetallesPago() { return detallesPago; }
    public boolean isPagoCompleto() { return pagoCompleto; }
}