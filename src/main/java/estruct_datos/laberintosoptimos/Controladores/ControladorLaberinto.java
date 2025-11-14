package estruct_datos.laberintosoptimos.Controladores;

import estruct_datos.laberintosoptimos.Laberinto.CeldaLaberinto;
import estruct_datos.laberintosoptimos.Laberinto.GeneradorLaberinto;
import estruct_datos.laberintosoptimos.Laberinto.Laberinto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class ControladorLaberinto {
    @FXML
    private Canvas canvas;

    @FXML
    private StackPane contenedorCanvas;

    @FXML
    private Spinner<Integer> anchoSpinner, altoSpinner;

    @FXML
    private Button generarLab;

    private final GeneradorLaberinto generador = new GeneradorLaberinto();
    private Laberinto laberinto;

    private Image imaPared;
    private Image imaSuelo;
    private Image imaPersonaje;
    private Image imaMeta;

    //Posiciones para el personaje y la meta (-1 es que no están colocados)
    private int personajeX = -1;
    private int personajeY = -1;
    private int metaX = -1;
    private int metaY = -1;

    @FXML
    public void initialize() {
        //Configura los spinners (valor mínimo, máximo y valor inicial)
        anchoSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 100, 15));
        altoSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 100, 15));

        //Define imágenes
        imaPared = new Image(Objects.requireNonNull(getClass().getResource("/Imagenes/Pared.jpg")).toExternalForm());
        imaSuelo = new Image(Objects.requireNonNull(getClass().getResource("/Imagenes/Suelo.jpg")).toExternalForm());
        imaPersonaje = new Image(Objects.requireNonNull(getClass().getResource("/Imagenes/Personaje.jpg")).toExternalForm());
        imaMeta = new Image(Objects.requireNonNull(getClass().getResource("/Imagenes/Meta.jpg")).toExternalForm());

        //Redimensiona el tamaño del laberinto
        canvas.widthProperty().bind(contenedorCanvas.widthProperty());
        canvas.heightProperty().bind(contenedorCanvas.heightProperty());

        //Genera el laberinto en valores iniciales
        Platform.runLater(() -> {
            laberinto = generador.generar(anchoSpinner.getValue(), altoSpinner.getValue());
            dibujarLaberinto();
        });

        //Genera laberinto al presionar el botón
        generarLab.setOnAction(e -> {
            //Quita la meta y el personaje
            personajeX = -1;
            personajeY = -1;
            metaX = -1;
            metaY = -1;

            laberinto = generador.generar(anchoSpinner.getValue(), altoSpinner.getValue());
            dibujarLaberinto();
        });

        //Maneja el evento al clickear canvas
        canvas.setOnMouseClicked(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();

            //Calcula en qué celda se hizo clic
            double anchoCelda = canvas.getWidth() / laberinto.getAncho();
            double altoCelda = canvas.getHeight() / laberinto.getAlto();

            int celdaX = (int) (mouseX / anchoCelda);
            int celdaY = (int) (mouseY / altoCelda);

            //Verifica que la celda esté dentro del canvas
            if (celdaX >= 0  && celdaX < laberinto.getAncho() && celdaY >= 0 && celdaY < laberinto.getAlto()) {
                CeldaLaberinto celda = laberinto.getCelda(celdaX, celdaY); //Obtiene la referencia de la celda
                if (!celda.isPared()) {
                    //Clic izquierdo para personaje, derecho para meta
                    if (event.getButton() == MouseButton.PRIMARY) {
                        //Verifica que no se pongan en la misma casilla
                        if (celdaX == metaX && celdaY == metaY) {
                            return;
                        } else {
                            personajeX = celdaX;
                            personajeY = celdaY;
                        }
                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        if (celdaX == personajeX && celdaY == personajeY) {
                            return;
                        } else {
                            metaX = celdaX;
                            metaY = celdaY;
                        }
                    }
                    dibujarLaberinto();
                }
            }
        });
    }

    private void dibujarLaberinto() {
        GraphicsContext g = canvas.getGraphicsContext2D();

        //Divide el ancho y alto del canvas entre el ancho y alto de la matriz para saber cuantos px corresponden por celda
        double anchoCelda = canvas.getWidth() / laberinto.getAncho();
        double altoCelda = canvas.getHeight() / laberinto.getAlto();

        //Limpia el canvas para hacer otro laberinto
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        //Recorre el laberinto poniéndole imágen a las paredes y pasillos
        for (int y = 0; y < laberinto.getAlto(); y++) {
            for (int x = 0; x < laberinto.getAncho(); x++) {
                CeldaLaberinto celda = laberinto.getCelda(x, y);
                Image imagen;
                if (celda.isPared()) {
                    imagen = imaPared;
                } else {
                    imagen = imaSuelo;
                }
                g.drawImage(imagen, x * anchoCelda, y * altoCelda, anchoCelda, altoCelda);
            }
        }

        //Dibuja el personaje y la meta
        if (personajeX != -1 && personajeY != -1) {
            //Si la celda de arriba es pared, rota la imagen hasta que vea a un pasillo
            if (personajeY - 1 == -1 || laberinto.getCelda(personajeX, personajeY - 1).isPared()) {
                g.save();
                //El punto de rotación debe ser el centro del personaje
                g.translate((personajeX * anchoCelda) + anchoCelda / 2, (personajeY * altoCelda) + altoCelda / 2);

                if (personajeX + 1 != laberinto.getAncho() && !laberinto.getCelda(personajeX + 1, personajeY).isPared()) {
                    g.rotate(90);
                    g.drawImage(imaPersonaje, -altoCelda / 2, -anchoCelda / 2, altoCelda, anchoCelda);
                } else if (!laberinto.getCelda(personajeX, personajeY + 1).isPared()) {
                    g.rotate(180);
                    g.drawImage(imaPersonaje, -anchoCelda / 2, -altoCelda / 2, anchoCelda, altoCelda);
                } else {
                    g.rotate(-90);
                    g.drawImage(imaPersonaje, -altoCelda / 2, -anchoCelda / 2, altoCelda, anchoCelda);
                }

                g.restore();
            } else {
                g.drawImage(imaPersonaje, personajeX * anchoCelda, personajeY * altoCelda, anchoCelda, altoCelda);
            }
        }

        if (metaX != -1 && metaY != -1) {
            g.drawImage(imaMeta, metaX * anchoCelda, metaY * altoCelda, anchoCelda, altoCelda);
        }
    }
}