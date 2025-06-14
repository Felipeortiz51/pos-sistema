package com.tuempresa.pos.model;

public class Usuario {
    private int id;
    private String nombreUsuario;
    private String hashContrasena;
    private String rol;

    public enum Rol {
        ADMIN("admin"),
        CAJERO("cajero");

        private final String nombre;

        Rol(String nombre) {
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getHashContrasena() { return hashContrasena; }
    public void setHashContrasena(String hashContrasena) { this.hashContrasena = hashContrasena; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}