package battleship.opposingthings;

/**
 * Repräsentiert den Ort eines Feldes vom Gegner
 */
public class OpposingField {
    /**
     * X-Koordinate des Feldes, beginnend bei 0
     */
    private final int x;
    /**
     * Y-Koordinate des Feldes, beginnend bei 0
     */
    private final int y;

    /**
     * Erstellt eine neue Ortsangabe für ein Feld des Gegners
     *
     * @param x X-Koordinate des Feldes, beginnend bei 0
     * @param y Y-Koordinate des Feldes, beginnend bei 0
     */
    public OpposingField(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gibt die X-Koordinate des Feldes zurück.
     *
     * @return X-Koordinate beginned bei 0
     */
    public int getX() {
        return this.x;
    }

    /**
     * Gibt die Y-Koordinate des Feldes zurück.
     *
     * @return Y-Koordinate beginned bei 0
     */
    public int getY() {
        return this.y;
    }

    @Override
    public String toString() {
        return "OpposingField [x=" + this.x + ", y=" + this.y + "]";
    }
}
