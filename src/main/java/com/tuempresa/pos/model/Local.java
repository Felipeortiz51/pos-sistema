package com.tuempresa.pos.model;

public class Local {
    private int id;
    private String nombre;
    private String direccion;
    // Añadir otros campos si son necesarios

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    // Esto es muy útil para mostrar los locales en un ComboBox (selector)
    @Override
    public String toString() {
        return nombre;
    }
}