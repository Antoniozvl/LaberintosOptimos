package estruct_datos.laberintosoptimos.Laberinto;

public class CeldaLaberinto {
    private boolean pared;
    private int costo;

    public CeldaLaberinto(boolean pared, int costo) {
        this.pared = pared;
        this.costo = costo;
    }

    //Getters
    public boolean isPared() {
        return pared;
    }

    public int getCosto() {
        return costo;
    }

    //Setters
    public void setPared(boolean pared) {
        this.pared = pared;
    }

    public void setCosto(int costo) {
        this.costo = costo;
    }
}