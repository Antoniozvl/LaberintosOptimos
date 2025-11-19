package estruct_datos.laberintosoptimos.Laberinto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GeneradorLaberinto {
    private final Random rand = new Random();

    public Laberinto generar(int ancho, int alto) {
        //Las dimensiones deben ser impares para que siempre exista una pared entre pasillos
        if (ancho % 2 == 0) {
            ancho++;
        }
        if (alto % 2 == 0) {
            alto++;
        }

        Laberinto laberinto = new Laberinto(ancho, alto);

        //Inicializamos todo como paredes
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                laberinto.setCelda(x, y, new CeldaLaberinto(true));
            }
        }

        //Punto inicial
        int inicioX = 1;
        int inicioY = 1;
        laberinto.getCelda(inicioX, inicioY).setPared(false);

        //Lista de paredes adyacentes a pasillos
        List<int[]> paredes = new ArrayList<>();
        agregarParedes(paredes, inicioX, inicioY, ancho, alto);

        while (!paredes.isEmpty()) {
            int[] pared = paredes.remove(rand.nextInt(paredes.size()));
            int x = pared[0];
            int y = pared[1];

            //Identifica la celda subsecuente a (x,y)
            int dx = pared[2];
            int dy = pared[3];
            int nx = x + dx;
            int ny = y + dy;

            //Sí la celda subsecuente está dentro del laberinto y no se ha modificado(es pared)
            if (nx > 0 && nx < ancho && ny > 0 && ny < alto && laberinto.getCelda(nx, ny).isPared()) {
                laberinto.getCelda(x, y).setPared(false);
                laberinto.getCelda(nx, ny).setPared(false);

                agregarParedes(paredes, nx, ny, ancho, alto);
            }
        }

        //Generar entrada y salida aleatoria
        List<int[]> posibles = new ArrayList<>();

        //Recorre borde superior e inferior buscando posibles lugares de entrada/salida
        for (int x = 1; x < ancho - 1; x++) {
            if (!laberinto.getCelda(x, 1).isPared()) {
                posibles.add(new int[]{x, 0});
            }
            if (!laberinto.getCelda(x, alto - 2).isPared()) {
                posibles.add(new int[]{x, alto - 1});
            }
        }

        //Recorre borde derecho e izquierdo buscando posibles lugares de entrada/salida
        for (int y = 1; y < alto - 1; y++) {
            if (!laberinto.getCelda(1, y).isPared()) {
                posibles.add(new int[]{0, y});
            }
            if (!laberinto.getCelda(ancho - 2, y).isPared()) {
                posibles.add(new int[]{ancho - 1, y});
            }
        }

        //Escoge una entrada y salida aleatoria de la lista de posibles
        int[] entrada = posibles.get(rand.nextInt(posibles.size()));
        int[] salida;
        do {
            salida = posibles.get(rand.nextInt(posibles.size()));
        } while (Arrays.equals(entrada, salida));

        laberinto.getCelda(entrada[0], entrada[1]).setPared(false);
        laberinto.getCelda(salida[0], salida[1]).setPared(false);

        return laberinto;
    }

    //Agrega las paredes de alrededor de la celda dada a la lista de paredes
    private void agregarParedes(List<int[]> paredes, int x, int y, int ancho, int alto) {
        //Pared izquierda
        if (x >= 2) {
            paredes.add(new int[]{x - 1, y, -1, 0});
        }
        //Pared derecha
        if (x < ancho - 2) {
            paredes.add(new int[]{x + 1, y, 1, 0});
        }
        //Pared superior
        if (y >= 2) {
            paredes.add(new int[]{x, y - 1, 0, -1});
        }
        //Pared inferior
        if (y < alto - 2) {
            paredes.add(new int[]{x, y + 1, 0, 1});
        }
    }
}