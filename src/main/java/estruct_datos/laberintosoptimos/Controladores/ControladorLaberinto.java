package estruct_datos.laberintosoptimos.Controladores;

import estruct_datos.laberintosoptimos.Laberinto.CeldaLaberinto;
import estruct_datos.laberintosoptimos.Laberinto.GeneradorLaberinto;
import estruct_datos.laberintosoptimos.Laberinto.Laberinto;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ControladorLaberinto {
    @FXML
    private Canvas canvas;

    @FXML
    private StackPane contenedorCanvas;

    @FXML
    private Spinner<Integer> anchoSpinner, altoSpinner;

    @FXML
    private Button generarLab, rutaOptima;

    @FXML
    private TextArea consolaInfo;

    private final GeneradorLaberinto generador = new GeneradorLaberinto();
    private Laberinto laberinto;

    //Determina si hay una animación en curso
    private boolean animacion = false;

    //Almacenan el tiempo (hora actual) de inicio y de fin
    private long tiempoInicio, tiempoFin;

    private Image imaPared;
    private Image imaSuelo;
    private Image imaMeta;
    private Image imaPersonaje;
    private Image imaPisada, imaVuelta;

    //Lista de coordenadas para la ruta
    private final List<int[]> ruta = new ArrayList<>();

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
        imaMeta = new Image(Objects.requireNonNull(getClass().getResource("/Imagenes/Meta.jpg")).toExternalForm());
        imaPersonaje = new Image(Objects.requireNonNull(getClass().getResource("/Imagenes/Personaje.png")).toExternalForm());
        imaPisada = new Image(Objects.requireNonNull(getClass().getResource("/Imagenes/Pisada.png")).toExternalForm());
        imaVuelta = new Image(Objects.requireNonNull(getClass().getResource("/Imagenes/Vuelta.png")).toExternalForm());

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
            //Si hay una animación en proceso no hace nada
            if (animacion) {
                return;
            }

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
            //Si hay una animación en proceso no hace nada
            if (animacion) {
                return;
            }

            //Obtiene la posición del mouse
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

        //Genera la ruta óptima mediante algoritmo A*
        rutaOptima.setOnAction(e -> {
            //La meta y el personaje deben estar en el tablero
            if (personajeX != -1 && metaX != -1 && !animacion) {
                //Si están en la misma casilla, retorna (evita problemas después de 1ra ejecución)
                if (personajeX == metaX && personajeY == metaY) {
                    return;
                }
                
                tiempoInicio = System.currentTimeMillis();
                animacion = true;
                consolaInfo.setText("Buscando ruta...");
                mostrarMatriz();

                PauseTransition delay = new PauseTransition(Duration.seconds(0.2));
                delay.setOnFinished(event -> algoritmoEstrella());
                delay.play();
            } else if (!animacion) { //Genera al personaje y la meta de forma aleatoria
                Random random = new Random();

                //Añade a la lista todas las celdas que sean pasillo
                List<int[]> pasillos = new ArrayList<>();
                for (int y = 0; y < laberinto.getAlto(); y++) {
                    for (int x = 0; x < laberinto.getAncho(); x++) {
                        if (!laberinto.getCelda(x,y).isPared()) {
                            pasillos.add(new int[]{x, y});
                        }
                    }
                }

                //Coloca el personaje en una celda aleatoria
                int indicePersonaje = random.nextInt(pasillos.size());
                int[] posicionPersonaje = pasillos.get(indicePersonaje);
                personajeX = posicionPersonaje[0];
                personajeY = posicionPersonaje[1];

                //Colocar meta en celda aleatoria diferente al personaje
                int indiceMeta;
                do {
                    indiceMeta = random.nextInt(pasillos.size());
                } while (indiceMeta == indicePersonaje);

                int[] posicionMeta = pasillos.get(indiceMeta);
                metaX = posicionMeta[0];
                metaY = posicionMeta[1];

                //Muestra el personaje y la meta
                dibujarLaberinto();
            }
        });
    }

    private void algoritmoEstrella() {
        ruta.clear();
        GraphicsContext g = canvas.getGraphicsContext2D();
        double anchoCelda = canvas.getWidth() / laberinto.getAncho();
        double altoCelda = canvas.getHeight() / laberinto.getAlto();

        //Lista de nodos recorridos o por recorrer
        List<Nodo> openSet  = new ArrayList<>();
        List<Nodo> closedSet  = new ArrayList<>();

        //Crea el nodo inicial
        Nodo inicio = new Nodo(personajeX, personajeY, null, 0, Math.abs(personajeX - metaX) + Math.abs(personajeY - metaY));
        //Añade el nodo inicio a los abiertos y cambia el color
        openSet.add(inicio);
        g.setFill(Color.RED);
        g.fillRect(inicio.x * anchoCelda, inicio.y * altoCelda, anchoCelda, altoCelda);

        PauseTransition delay = new PauseTransition(Duration.seconds(0.2));
        delay.setOnFinished(event -> {
            //Encuentra el nodo con mejor costoF
            Nodo actual = openSet.get(0);
            for (Nodo nodo : openSet) {
                if (nodo.costoF < actual.costoF) {
                    actual = nodo;
                }
            }

            //Mueve el nodo actual de openSet a closedSet
            openSet.remove(actual);
            closedSet.add(actual);
            g.setFill(Color.GREEN);
            g.fillRect(actual.x * anchoCelda, actual.y * altoCelda, anchoCelda, altoCelda);

            //Si es la meta, reconstruye el camino
            if (actual.x == metaX && actual.y == metaY) {
                reconstruirCamino(actual);

                PauseTransition delay2 = new PauseTransition(Duration.seconds(0.2));
                delay2.setOnFinished(e -> {
                    //Dibuja la ruta en la matriz
                    mostrarMatriz();
                    g.setFill(Color.BLUE);

                    //Imprime las coordenadas de la ruta
                    System.out.println("\nLista de coordenadas");
                    for (int[] pasos : ruta) {
                        System.out.print("(" + pasos[0] + "," + pasos[1] + ") -> ");
                        g.fillRect(pasos[0] * anchoCelda, pasos[1] * altoCelda, anchoCelda, altoCelda);
                    }

                    consolaInfo.setText("¡Ruta encontrada!\nLongitud: " + (ruta.size() - 1) + " pasos.");

                    //Animación de personaje hacia meta
                    PauseTransition delay3 = new PauseTransition(Duration.seconds(0.5));
                    delay3.setOnFinished(ev -> movimientoPersonaje());
                    delay3.play();
                });
                delay2.play();
                return;
            }

            //Explora a los vecinos
            for (int[] direccion : new int[][]{{0, -1}, {1, 0}, {0, 1}, {-1, 0}}) { //Arriba, derecha, abajo, izquierda
                int nuevoX = actual.x + direccion[0];
                int nuevoY = actual.y + direccion[1];

                //Verifica si está dentro del laberinto y no es pared
                Nodo vecino;
                if (nuevoX >= 0 && nuevoX < laberinto.getAncho() && nuevoY >= 0 && nuevoY < laberinto.getAlto() && !laberinto.getCelda(nuevoX, nuevoY).isPared()) {
                    vecino = new Nodo(nuevoX, nuevoY, actual, actual.costoG + 1, Math.abs(nuevoX - metaX) + Math.abs(nuevoY - metaY));
                } else {
                    continue;
                }

                //Si ya está en closedSet, continúa con el siguiente vecino
                if (closedSet.contains(vecino)) {
                    continue;
                }

                //Si no está en abiertos, lo agrega
                if (!openSet.contains(vecino)) {
                    openSet.add(vecino);
                    g.setFill(Color.RED);
                    g.fillRect(vecino.x * anchoCelda, vecino.y * altoCelda, anchoCelda, altoCelda);
                }
            }
            delay.play();
        });
        delay.play();
    }

    private void movimientoPersonaje() {
        //Pasa de la matriz al laberinto
        dibujarLaberinto();

        GraphicsContext g = canvas.getGraphicsContext2D();
        double anchoCelda = canvas.getWidth() / laberinto.getAncho();
        double altoCelda = canvas.getHeight() / laberinto.getAlto();

        //Inicia movimiento en la primer coordenada
        moverCasilla(0, g, anchoCelda, altoCelda);
    }

    private void moverCasilla(int contador, GraphicsContext g, double anchoCelda, double altoCelda) {
        //Si es la meta, termina la animación
        if (contador == ruta.size() - 1) {
            tiempoFin = System.currentTimeMillis();

            //Convierte los ms tardados a seg
            double tiempoSeg = (double) (tiempoFin - tiempoInicio) / 1000;
            //Limita a 2 decimales
            String tiempo = String.format("%.2f", tiempoSeg);

            consolaInfo.setText("¡El personaje llegó a la \nmeta!\n\nLongitud: " + (ruta.size() - 1) + " pasos.\n\nTiempo: " + tiempo + "seg.");
            animacion = false;
            return;
        }

        PauseTransition delay = new PauseTransition(Duration.seconds(0.2));
        delay.setOnFinished(actionEvent -> {
            //Borra al personaje dibujando el suelo, dibujando el suelo
            g.drawImage(imaSuelo, personajeX * anchoCelda, personajeY * altoCelda, anchoCelda, altoCelda);

            //Determina la dirección del movimiento actual
            int[] posActual = ruta.get(contador);
            int[] posSiguiente = ruta.get(contador + 1);

            int direccionX = posSiguiente[0] - posActual[0];
            int direccionY = posSiguiente[1] - posActual[1];

            //Determina si es un vértice (vuelta)
            boolean vertice = false;
            if (contador > 0) {
                int[] posAnterior = ruta.get(contador - 1);
                int direccionAnteriorX = posActual[0] - posAnterior[0];
                int direccionAnteriorY = posActual[1] - posAnterior[1];

                if (direccionX != direccionAnteriorX || direccionY != direccionAnteriorY) {
                    vertice = true;
                    //Dibuja la vuelta
                    rotarVuelta(g, anchoCelda, altoCelda, direccionAnteriorX, direccionAnteriorY, direccionX, direccionY);
                }
            }

            //Dibuja la pisada
            if (!vertice) {
                rotarYDibujar(imaPisada, g, anchoCelda, altoCelda, direccionX, direccionY);
            }

            //Mueve al personaje a la siguiente casilla de la ruta
            personajeX = ruta.get(contador + 1)[0];
            personajeY = ruta.get(contador + 1)[1];

            //Dibuja al personaje rotado según la dirección
            rotarYDibujar(imaPersonaje, g, anchoCelda, altoCelda, direccionX, direccionY);

            //Llama de nuevo a la función para la siguiente coordenada
            moverCasilla(contador + 1, g, anchoCelda, altoCelda);
        });
        delay.play();
    }

    private void rotarVuelta(GraphicsContext g, double anchoCelda, double altoCelda, int direccionAnteriorX, int direccionAnteriorY, int direccionX, int direccionY) {
        g.save();

        //Calcula el centro de la imagen
        double centroX = personajeX * anchoCelda + anchoCelda / 2;
        double centroY = personajeY * altoCelda + altoCelda / 2;

        g.translate(centroX, centroY);

        //Grados a rotar
        double grados = 0; //Derecha -> abajo ó arriba -> izquierda (predeterminado)
        if (direccionAnteriorX == 1 && direccionY == -1 || //Derecha -> arriba
                direccionAnteriorY == 1 && direccionX == -1) { //Abajo -> izquierda
            grados = 90;
        } else if (direccionAnteriorY == -1 && direccionX == 1 || //Arriba -> derecha
                direccionAnteriorX == -1 && direccionY == 1) { //Izquierda -> abajo
            grados = -90;
        } else if (direccionAnteriorX == -1 && direccionY == -1 || //Izquierda -> arriba
                    direccionAnteriorY == 1 && direccionX == 1) { //Abajo -> derecha
            grados = 180;
        }

        g.rotate(grados);

        //Dibuja según rotación
        if (grados == 90 || grados == -90) {
            g.drawImage(imaVuelta, -altoCelda / 2, -anchoCelda / 2, altoCelda, anchoCelda);
        } else {
            g.drawImage(imaVuelta, -anchoCelda / 2, -altoCelda / 2, anchoCelda, altoCelda);
        }

        g.restore();
    }

    private void rotarYDibujar(Image imagen, GraphicsContext g, double anchoCelda, double altoCelda, int direccionX, int direccionY) {
        g.save();

        //Calcula el centro de la imagen
        double centroX = personajeX * anchoCelda + anchoCelda / 2;
        double centroY = personajeY * altoCelda + altoCelda / 2;

        g.translate(centroX, centroY);

        //Grados a rotar
        double grados = 0; //Arriba (predeterminado)
        if (direccionX == 1) { //Derecha
            grados = 90;
        } else if (direccionX == -1) { //Izquierda
            grados = -90;
        } else if (direccionY == 1) { //Abajo
            grados = 180;
        }

        g.rotate(grados);

        //Dibuja según rotación
        if (grados == 90 || grados == -90) {
            g.drawImage(imagen, -altoCelda / 2, -anchoCelda / 2, altoCelda, anchoCelda);
        } else {
            g.drawImage(imagen, -anchoCelda / 2, -altoCelda / 2, anchoCelda, altoCelda);
        }

        g.restore();
    }

    private void reconstruirCamino(Nodo nodoFinal) {
        Nodo actual = nodoFinal;

        //Mientras existan padres, agregar a la ruta
        while (actual != null) {
            //Añade la coordenada al inicio de la lista para invertir el orden y tenerlo de inicio a fin
            ruta.add(0, new int[]{actual.x, actual.y});
            actual = actual.padre;
        }
    }

    private void mostrarMatriz() {
        GraphicsContext g = canvas.getGraphicsContext2D();

        //Divide el ancho y alto del canvas entre el ancho y alto de la matriz para saber cuantos px corresponden por celda
        double anchoCelda = canvas.getWidth() / laberinto.getAncho();
        double altoCelda = canvas.getHeight() / laberinto.getAlto();

        //Limpia el canvas
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        //Dibuja laberinto con transparencia
        g.setGlobalAlpha(0.3); //30% opacidad
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

        g.setGlobalAlpha(1.0); //Restaura opacidad
        //Dibujar matriz
        for (int y = 0; y < laberinto.getAlto(); y++) {
            for (int x = 0; x < laberinto.getAncho(); x++) {
                CeldaLaberinto celda = laberinto.getCelda(x, y);

                //Dibujar borde de la celda
                g.setStroke(Color.GRAY);
                g.setLineWidth(1);
                g.strokeRect(x * anchoCelda, y * altoCelda, anchoCelda, altoCelda);

                //Dibujar valor
                g.setFill(Color.BLACK);
                if (celda.isPared()) {
                    g.fillText("1", x * anchoCelda + anchoCelda / 2 - 4, y * altoCelda + altoCelda / 2 + 5);
                } else {
                    g.fillText("0", x * anchoCelda + anchoCelda / 2 - 4, y * altoCelda + altoCelda / 2 + 5);
                }
            }
        }

        //Dibujar personaje en verde (openSet)
        g.setFill(Color.GREEN);
        g.fillRect(personajeX * anchoCelda, personajeY * altoCelda, anchoCelda, altoCelda);

        //Dibujar meta en azul
        g.setFill(Color.BLUE);
        g.fillRect(metaX * anchoCelda, metaY * altoCelda, anchoCelda, altoCelda);

        g.setGlobalAlpha(1.0);
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

        //Actualiza consolaInfo
        if (metaX == -1 || personajeX == -1) {
            consolaInfo.setText("\nLaberinto generado.\nColoca el personaje \n(clic izquierdo) y la \nmeta (clic derecho) \npara poder generar la \nruta óptima, de lo \ncontrario se \nestablecerán de \nforma aleatoria al \npresionar el botón.");
        } else if (ruta.isEmpty()) {
            consolaInfo.setText("\nLaberinto generado y \nlisto para generar \nruta óptima.");
        }
    }
}