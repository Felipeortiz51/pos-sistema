package com.tuempresa.pos.util;

import javafx.geometry.Pos;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class NotificationUtil {

    /**
     * Muestra una notificación de información (azul).
     * @param titulo El título de la notificación.
     * @param mensaje El cuerpo del mensaje.
     */
    public static void mostrarInformacion(String titulo, String mensaje) {
        Notifications notificationBuilder = Notifications.create()
                .title(titulo)
                .text(mensaje)
                .graphic(null) // Puedes añadir un ícono si quieres
                .hideAfter(Duration.seconds(5)) // Desaparece después de 5 segundos
                .position(Pos.BOTTOM_RIGHT); // Posición en la pantalla
        notificationBuilder.showInformation();
    }

    /**
     * Muestra una notificación de error (roja).
     * @param titulo El título de la notificación.
     * @param mensaje El cuerpo del mensaje.
     */
    public static void mostrarError(String titulo, String mensaje) {
        Notifications notificationBuilder = Notifications.create()
                .title(titulo)
                .text(mensaje)
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.BOTTOM_RIGHT);
        notificationBuilder.showError();
    }

    /**
     * Muestra una notificación de advertencia (amarilla).
     * @param titulo El título de la notificación.
     * @param mensaje El cuerpo del mensaje.
     */
    public static void mostrarAdvertencia(String titulo, String mensaje) {
        Notifications notificationBuilder = Notifications.create()
                .title(titulo)
                .text(mensaje)
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.BOTTOM_RIGHT);
        notificationBuilder.showWarning();
    }
}