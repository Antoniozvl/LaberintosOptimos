package estruct_datos.laberintosoptimos.Laberinto;

public class CeldaLaberinto {
    private boolean pared;

    public CeldaLaberinto(boolean pared) {
        this.pared = pared;
    }

    //Getters
    public boolean isPared() {
        return pared;
    }

    //Setters
    public void setPared(boolean pared) {
        this.pared = pared;
    }
}