package com.tuempresa.pos.service;

import com.tuempresa.pos.model.Usuario;

public class SessionManager {

    private static SessionManager instance;
    private Usuario usuarioActual;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    public boolean isAdmin() {
        return usuarioActual != null && "admin".equalsIgnoreCase(usuarioActual.getRol());
    }
}