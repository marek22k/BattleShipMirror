package battleship.ui.playgroundmatrix;

/**
 * Ereignis, welches das Feuern auf ein Feld beschreibt, indem es die X- und
 * Y-Koordinaten des Feldes speichert.
 */
public class FireEvent {
    /*
     * X-Koordinate
     */
    private final int x;
    /*
     * Y-Koordinate
     */
    private final int y;

    /**
     * Erstellt ein neues Ereignis
     *
     * @param x X-Koordinate des Feldes, auf das gefeuert wurde
     * @param y Y-Koordinate des Feldes, auf das gefeuert wurde
     */
    public FireEvent(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gibt die X-Koordinate vom Feld, auf welches gefeuert wurde, zurück.
     *
     * @return X-Koordinate
     */
    public int getX() {
        return this.x;
    }

    /**
     * Gibt die Y-Koordinate vom Feld, auf welches gefeuert wurde, zurück.
     *
     * @return Y-Koordinate
     */
    public int getY() {
        return this.y;
    }
}
