package com.tuempresa.pos;

import com.tuempresa.pos.config.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Inicializa la base de datos al inicio
            DatabaseManager.getConnection();

            // La aplicación ahora comienza con la vista de Login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setTitle("Inicio de Sesión - Sistema POS");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error fatal al cargar la aplicación.");
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Cierra la conexión a la base de datos al cerrar la app
        DatabaseManager.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}