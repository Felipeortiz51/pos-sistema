package com.tuempresa.pos.controller;

import com.tuempresa.pos.dao.ProductoDAO;
import com.tuempresa.pos.dao.VentaDAO;
import com.tuempresa.pos.service.SessionManager;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DashboardContentController {

    @FXML private Label lblProductosActivos;
    @FXML private Label lblVentasHoy;
    @FXML private Label lblVentasSubtitulo;
    @FXML private Label lblAlertasStock;
    @FXML private Button btnAccesoRapidoVentas;
    @FXML private BarChart<String, Number> barChartVentas;
    @FXML private GridPane dashboardGrid;

    private ProductoDAO productoDAO;
    private VentaDAO ventaDAO;

    @FXML
    public void initialize() {
        productoDAO = new ProductoDAO();
        ventaDAO = new VentaDAO();

        // Animar entrada del dashboard
        animarEntradaDashboard();

        // Cargar y animar datos
        cargarDatosDashboard();
        cargarDatosGrafico();

        // Configurar animaciones de hover para botones
        setupButtonAnimations();
    }

    /**
     * Animación de entrada del dashboard con tarjetas apareciendo una por una
     */
    private void animarEntradaDashboard() {
        // Obtener todas las tarjetas del grid
        for (int i = 0; i < dashboardGrid.getChildren().size(); i++) {
            javafx.scene.Node tarjeta = dashboardGrid.getChildren().get(i);

            // Ocultar inicialmente
            tarjeta.setOpacity(0);
            tarjeta.setTranslateY(50);
            tarjeta.setScaleX(0.8);
            tarjeta.setScaleY(0.8);

            // Calcular delay progresivo
            Duration delay = Duration.millis(i * 200);

            // Animación de entrada
            FadeTransition fade = new FadeTransition(Duration.millis(600), tarjeta);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setDelay(delay);

            TranslateTransition slide = new TranslateTransition(Duration.millis(600), tarjeta);
            slide.setFromY(50);
            slide.setToY(0);
            slide.setDelay(delay);
            slide.setInterpolator(Interpolator.EASE_OUT);

            ScaleTransition scale = new ScaleTransition(Duration.millis(600), tarjeta);
            scale.setFromX(0.8);
            scale.setFromY(0.8);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.setDelay(delay);
            scale.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition entrada = new ParallelTransition(fade, slide, scale);
            entrada.play();

            // Configurar hover para las tarjetas
            setupTarjetaHover(tarjeta);
        }
    }

    /**
     * Configurar efecto hover para las tarjetas
     */
    private void setupTarjetaHover(javafx.scene.Node tarjeta) {
        tarjeta.setOnMouseEntered(e -> {
            // Hover: elevar la tarjeta
            ScaleTransition hoverScale = new ScaleTransition(Duration.millis(200), tarjeta);
            hoverScale.setToX(1.02);
            hoverScale.setToY(1.02);
            hoverScale.setInterpolator(Interpolator.EASE_OUT);

            // Agregar sombra más intensa
            DropShadow hoverShadow = new DropShadow();
            hoverShadow.setColor(Color.web("#000000", 0.15));
            hoverShadow.setRadius(20);
            hoverShadow.setOffsetY(8);
            tarjeta.setEffect(hoverShadow);

            hoverScale.play();
        });

        tarjeta.setOnMouseExited(e -> {
            // Volver al estado normal
            ScaleTransition normalScale = new ScaleTransition(Duration.millis(200), tarjeta);
            normalScale.setToX(1.0);
            normalScale.setToY(1.0);
            normalScale.setInterpolator(Interpolator.EASE_OUT);

            // Sombra normal
            DropShadow normalShadow = new DropShadow();
            normalShadow.setColor(Color.web("#000000", 0.08));
            normalShadow.setRadius(12);
            normalShadow.setOffsetY(4);
            tarjeta.setEffect(normalShadow);

            normalScale.play();
        });
    }

    /**
     * Configurar animaciones para botones
     */
    private void setupButtonAnimations() {
        if (btnAccesoRapidoVentas != null) {
            // Efecto hover para el botón principal
            btnAccesoRapidoVentas.setOnMouseEntered(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), btnAccesoRapidoVentas);
                scale.setToX(1.05);
                scale.setToY(1.05);
                scale.play();

                DropShadow shadow = new DropShadow();
                shadow.setColor(Color.web("#0d6efd", 0.4));
                shadow.setRadius(15);
                shadow.setOffsetY(5);
                btnAccesoRapidoVentas.setEffect(shadow);
            });

            btnAccesoRapidoVentas.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), btnAccesoRapidoVentas);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();

                btnAccesoRapidoVentas.setEffect(null);
            });

            // Efecto click
            btnAccesoRapidoVentas.setOnMousePressed(e -> {
                ScaleTransition press = new ScaleTransition(Duration.millis(100), btnAccesoRapidoVentas);
                press.setToX(0.95);
                press.setToY(0.95);
                press.play();
            });

            btnAccesoRapidoVentas.setOnMouseReleased(e -> {
                ScaleTransition release = new ScaleTransition(Duration.millis(100), btnAccesoRapidoVentas);
                release.setToX(1.05);
                release.setToY(1.05);
                release.play();
            });
        }
    }

    public void setNavegacionVentasAction(EventHandler<ActionEvent> action) {
        if (btnAccesoRapidoVentas != null) {
            btnAccesoRapidoVentas.setOnAction(e -> {
                // Animación de click antes de navegar
                animarClickNavegacion(() -> action.handle(e));
            });
        }
    }

    /**
     * Animación de click para navegación
     */
    private void animarClickNavegacion(Runnable action) {
        // Efecto de "onda" que se expande desde el botón
        ScaleTransition ripple = new ScaleTransition(Duration.millis(300), btnAccesoRapidoVentas);
        ripple.setFromX(1.0);
        ripple.setFromY(1.0);
        ripple.setToX(1.1);
        ripple.setToY(1.1);
        ripple.setCycleCount(2);
        ripple.setAutoReverse(true);

        ripple.setOnFinished(e -> action.run());
        ripple.play();
    }

    private void cargarDatosDashboard() {
        // --- INICIO DE CAMBIOS ---
        // Obtenemos el ID del local activo
        int idLocal = SessionManager.getInstance().getLocalId();

        // Llamamos a los métodos del DAO con el ID del local
        int productos = productoDAO.contarProductosActivos(idLocal);
        int alertas = productoDAO.contarAlertasStock(5, idLocal);
        // Nota: Los métodos de VentaDAO también necesitarán ser actualizados en el futuro
        double ventasHoy = ventaDAO.sumarVentasHoy();
        double ventasAyer = ventaDAO.sumarVentasAyer();
        // --- FIN DE CAMBIOS ---

        // Animar contadores con efecto de contar desde 0
        animarContador(lblProductosActivos, 0, productos, "");
        animarContadorMoneda(lblVentasHoy, 0, ventasHoy);
        animarContador(lblAlertasStock, 0, alertas, "");

        // Configurar subtítulo de ventas
        configurarSubtituloVentas(ventasHoy, ventasAyer);
    }

    /**
     * Animar contador numérico
     */
    private void animarContador(Label label, int desde, int hasta, String sufijo) {
        Timeline contador = new Timeline();

        for (int i = 0; i <= 30; i++) {
            int valor = desde + (int)((hasta - desde) * (i / 30.0));
            KeyFrame frame = new KeyFrame(
                    Duration.millis(i * 50),
                    e -> label.setText(String.valueOf(valor) + sufijo)
            );
            contador.getKeyFrames().add(frame);
        }

        // Añadir efecto de "pop" al final
        contador.setOnFinished(e -> {
            ScaleTransition pop = new ScaleTransition(Duration.millis(200), label);
            pop.setFromX(1.0);
            pop.setFromY(1.0);
            pop.setToX(1.1);
            pop.setToY(1.1);
            pop.setCycleCount(2);
            pop.setAutoReverse(true);
            pop.play();
        });

        contador.play();
    }

    /**
     * Animar contador de dinero
     */
    private void animarContadorMoneda(Label label, double desde, double hasta) {
        Timeline contador = new Timeline();

        for (int i = 0; i <= 30; i++) {
            double valor = desde + ((hasta - desde) * (i / 30.0));
            KeyFrame frame = new KeyFrame(
                    Duration.millis(i * 50),
                    e -> label.setText(String.format("$%,.2f", valor))
            );
            contador.getKeyFrames().add(frame);
        }

        contador.setOnFinished(e -> {
            // Efecto especial para dinero - glow dorado
            DropShadow goldGlow = new DropShadow();
            goldGlow.setColor(Color.web("#FFD700", 0.6));
            goldGlow.setRadius(10);
            label.setEffect(goldGlow);

            // Quitar el glow después de 1 segundo
            Timeline removeGlow = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                label.setEffect(null);
            }));
            removeGlow.play();
        });

        contador.play();
    }

    /**
     * Configurar subtítulo de ventas con animación
     */
    private void configurarSubtituloVentas(double ventasHoy, double ventasAyer) {
        String textoSubtitulo;
        double porcentajeCambio = 0;

        lblVentasSubtitulo.getStyleClass().removeAll("card-subtitle", "card-subtitle-alert");

        if (ventasAyer > 0) {
            porcentajeCambio = ((ventasHoy - ventasAyer) / ventasAyer) * 100;
            if (porcentajeCambio >= 0) {
                textoSubtitulo = String.format("+%.0f%% que ayer", porcentajeCambio);
                lblVentasSubtitulo.getStyleClass().add("card-subtitle");

                // Animación de éxito (verde)
                animarCambioPositivo(lblVentasSubtitulo);
            } else {
                textoSubtitulo = String.format("%.0f%% que ayer", porcentajeCambio);
                lblVentasSubtitulo.getStyleClass().add("card-subtitle-alert");

                // Animación de alerta (rojo)
                animarCambioNegativo(lblVentasSubtitulo);
            }
        } else if (ventasHoy > 0) {
            textoSubtitulo = "Primeras ventas";
            lblVentasSubtitulo.getStyleClass().add("card-subtitle");
            animarCambioPositivo(lblVentasSubtitulo);
        } else {
            textoSubtitulo = "Sin movimiento";
            lblVentasSubtitulo.getStyleClass().add("card-subtitle");
        }

        // Animar cambio de texto
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), lblVentasSubtitulo);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            lblVentasSubtitulo.setText(textoSubtitulo);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), lblVentasSubtitulo);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    /**
     * Animación para cambios positivos
     */
    private void animarCambioPositivo(Label label) {
        DropShadow greenGlow = new DropShadow();
        greenGlow.setColor(Color.web("#28a745", 0.5));
        greenGlow.setRadius(8);
        label.setEffect(greenGlow);

        Timeline removeGlow = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            label.setEffect(null);
        }));
        removeGlow.play();
    }

    /**
     * Animación para cambios negativos
     */
    private void animarCambioNegativo(Label label) {
        DropShadow redGlow = new DropShadow();
        redGlow.setColor(Color.web("#dc3545", 0.5));
        redGlow.setRadius(8);
        label.setEffect(redGlow);

        Timeline removeGlow = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            label.setEffect(null);
        }));
        removeGlow.play();
    }

    /**
     * Cargar y animar datos del gráfico
     */
    private void cargarDatosGrafico() {
        Map<LocalDate, Double> ventasPorDia = ventaDAO.getVentasDeUltimos7Dias();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventas Diarias");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM");

        // Crear datos del gráfico
        for (int i = 6; i >= 0; i--) {
            LocalDate fecha = LocalDate.now().minusDays(i);
            double ventas = ventasPorDia.getOrDefault(fecha, 0.0);
            series.getData().add(new XYChart.Data<>(fecha.format(formatter), ventas));
        }

        barChartVentas.getData().clear();
        barChartVentas.getData().add(series);

        // Animar las barras del gráfico una por una
        animarBarrasGrafico();
    }

    /**
     * Animar barras del gráfico apareciendo una por una
     */
    private void animarBarrasGrafico() {
        Timeline animacionBarras = new Timeline(new KeyFrame(Duration.millis(1500), e -> {
            // Buscar todas las barras del gráfico
            barChartVentas.lookupAll(".default-color0.chart-bar").forEach(node -> {
                // Ocultar inicialmente
                node.setScaleY(0);

                // Animar aparición
                ScaleTransition scaleBar = new ScaleTransition(Duration.millis(800), node);
                scaleBar.setFromY(0);
                scaleBar.setToY(1);
                scaleBar.setInterpolator(Interpolator.EASE_OUT);

                // Agregar un pequeño delay aleatorio para efecto escalonado
                scaleBar.setDelay(Duration.millis(Math.random() * 500));
                scaleBar.play();

                // Configurar hover para las barras
                setupBarraHover(node);
            });
        }));
        animacionBarras.play();
    }

    /**
     * Configurar efecto hover para barras del gráfico
     */
    private void setupBarraHover(javafx.scene.Node barra) {
        barra.setOnMouseEntered(e -> {
            ScaleTransition hoverScale = new ScaleTransition(Duration.millis(200), barra);
            hoverScale.setToX(1.1);
            hoverScale.setToY(1.05);
            hoverScale.play();

            // Cambiar color temporalmente
            barra.setStyle("-fx-bar-fill: #0b5ed7;");
        });

        barra.setOnMouseExited(e -> {
            ScaleTransition normalScale = new ScaleTransition(Duration.millis(200), barra);
            normalScale.setToX(1.0);
            normalScale.setToY(1.0);
            normalScale.play();

            // Restaurar color original
            barra.setStyle("-fx-bar-fill: #0d6efd;");
        });
    }

    /**
     * Animación de actualización de datos (para llamar cuando se actualicen los datos)
     */
    public void actualizarDatosConAnimacion() {
        // Fade out de todas las tarjetas
        for (javafx.scene.Node tarjeta : dashboardGrid.getChildren()) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), tarjeta);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.3);
            fadeOut.play();
        }

        // Esperar y luego recargar datos
        Timeline recargar = new Timeline(new KeyFrame(Duration.millis(400), e -> {
            cargarDatosDashboard();
            cargarDatosGrafico();

            // Fade in de vuelta
            for (javafx.scene.Node tarjeta : dashboardGrid.getChildren()) {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(400), tarjeta);
                fadeIn.setFromValue(0.3);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        }));
        recargar.play();
    }

    /**
     * Efecto de "pulso" para destacar una métrica importante
     */
    public void destacarMetrica(Label metrica) {
        ScaleTransition pulso1 = new ScaleTransition(Duration.millis(300), metrica);
        pulso1.setFromX(1.0);
        pulso1.setFromY(1.0);
        pulso1.setToX(1.15);
        pulso1.setToY(1.15);

        ScaleTransition pulso2 = new ScaleTransition(Duration.millis(300), metrica);
        pulso2.setFromX(1.15);
        pulso2.setFromY(1.15);
        pulso2.setToX(1.0);
        pulso2.setToY(1.0);

        SequentialTransition pulsoContinuo = new SequentialTransition(pulso1, pulso2);
        pulsoContinuo.setCycleCount(3);

        // Agregar glow especial
        DropShadow specialGlow = new DropShadow();
        specialGlow.setColor(Color.web("#ffc107", 0.8));
        specialGlow.setRadius(20);
        metrica.setEffect(specialGlow);

        pulsoContinuo.setOnFinished(e -> {
            metrica.setEffect(null);
        });

        pulsoContinuo.play();
    }

    /**
     * Animación de celebración cuando se alcanza una meta
     */
    public void animarCelebracion() {
        // Efecto de "confetti" simple usando las tarjetas
        for (int i = 0; i < dashboardGrid.getChildren().size(); i++) {
            javafx.scene.Node tarjeta = dashboardGrid.getChildren().get(i);

            // Animación de bounce
            ScaleTransition bounce = new ScaleTransition(Duration.millis(200), tarjeta);
            bounce.setFromX(1.0);
            bounce.setFromY(1.0);
            bounce.setToX(1.1);
            bounce.setToY(1.1);
            bounce.setCycleCount(2);
            bounce.setAutoReverse(true);
            bounce.setDelay(Duration.millis(i * 100));

            // Glow dorado
            DropShadow goldGlow = new DropShadow();
            goldGlow.setColor(Color.web("#FFD700", 0.8));
            goldGlow.setRadius(15);
            tarjeta.setEffect(goldGlow);

            bounce.setOnFinished(e -> {
                Timeline removeEffect = new Timeline(new KeyFrame(Duration.seconds(2), ev -> {
                    tarjeta.setEffect(null);
                }));
                removeEffect.play();
            });

            bounce.play();
        }
    }

    /**
     * Animación de carga para cuando se están obteniendo datos
     */
    public void mostrarAnimacionCarga() {
        for (javafx.scene.Node tarjeta : dashboardGrid.getChildren()) {
            // Efecto de "breathing" mientras carga
            FadeTransition breathe = new FadeTransition(Duration.millis(1000), tarjeta);
            breathe.setFromValue(1.0);
            breathe.setToValue(0.7);
            breathe.setCycleCount(Timeline.INDEFINITE);
            breathe.setAutoReverse(true);
            breathe.play();
        }
    }

    /**
     * Detener animación de carga
     */
    public void detenerAnimacionCarga() {
        for (javafx.scene.Node tarjeta : dashboardGrid.getChildren()) {
            tarjeta.setOpacity(1.0);
        }
    }
}