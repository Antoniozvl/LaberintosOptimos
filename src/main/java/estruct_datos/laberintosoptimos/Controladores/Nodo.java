package estruct_datos.laberintosoptimos.Controladores;

public class Nodo {
    protected int x, y;
    protected Nodo padre;
    protected double costoG; //Costo desde el inicio (pasos)
    protected double costoH; //Distancia estimada a la meta (Heurística)
    protected double costoF; // f = g + h

    public Nodo(int x, int y, Nodo padre, double costoG, double costoH) {
        this.x = x;
        this.y = y;
        this.padre = padre;
        this.costoG = costoG;
        this.costoH = costoH;
        this.costoF = costoG + costoH;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Nodo test = (Nodo) obj;
        return x == test.x && y == test.y;
    }
}