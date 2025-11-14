package estruct_datos.laberintosoptimos.Laberinto;

public class Laberinto {
    private final int ancho;
    private final int alto;
    private final CeldaLaberinto[][] cuadricula;

    public Laberinto(int ancho, int alto) {
        this.ancho = ancho;
        this.alto = alto;
        this.cuadricula = new CeldaLaberinto[alto][ancho];
    }

    //Getters
    public int getAncho() {
        return ancho;
    }

    public int getAlto() {
        return alto;
    }

    public CeldaLaberinto getCelda(int x, int y) {
        return cuadricula[y][x];
    }

    public CeldaLaberinto[][] getCuadricula() {
        return cuadricula;
    }

    //Setters
    public void setCelda(int x, int y, CeldaLaberinto celda) {
        this.cuadricula[y][x] = celda;
    }
}