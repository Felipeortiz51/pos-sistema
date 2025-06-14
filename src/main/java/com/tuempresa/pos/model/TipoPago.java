package com.tuempresa.pos.model;

public enum TipoPago {
    EFECTIVO("Efectivo", "💵", true),
    TARJETA_CREDITO("Tarjeta de Crédito", "💳", false),
    TARJETA_DEBITO("Tarjeta de Débito", "💳", false),
    TRANSFERENCIA("Transferencia", "🏦", false),
    MIXTO("Pago Mixto", "🔄", true);

    private final String nombre;
    private final String icono;
    private final boolean requiereCambio;

    TipoPago(String nombre, String icono, boolean requiereCambio) {
        this.nombre = nombre;
        this.icono = icono;
        this.requiereCambio = requiereCambio;
    }

    public String getNombre() { return nombre; }
    public String getIcono() { return icono; }
    public boolean isRequiereCambio() { return requiereCambio; }
}
