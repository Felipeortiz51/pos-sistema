package com.tuempresa.pos.service;

import com.tuempresa.pos.model.Local; // Importar el nuevo modelo
import com.tuempresa.pos.model.Usuario;
import java.util.List; // Importar List

public class SessionManager {

    private static SessionManager instance;
    private Usuario usuarioActual;
    private Local localActual; // <-- CAMPO AÑADIDO
    private List<String> permisos; // <-- Para el futuro sistema de permisos

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // --- Métodos para el Local Actual ---
    public void setLocalActual(Local local) {
        this.localActual = local;
    }

    public Local getLocalActual() {
        return this.localActual;
    }

    public int getLocalId() {
        if (localActual == null) {
            // Podríamos lanzar una excepción o devolver un valor por defecto.
            // Por seguridad, devolvemos 0 o -1 para que las consultas fallen
            // si no hay un local seleccionado.
            return 0;
        }
        return localActual.getId();
    }
    // ------------------------------------

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
        this.localActual = null; // Limpiar también el local al cerrar sesión
        this.permisos = null;
    }

    public boolean isAdmin() {
        return usuarioActual != null && "Administrador General".equalsIgnoreCase(usuarioActual.getRol());
    }
}