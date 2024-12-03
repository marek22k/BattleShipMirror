package battleship.playersThings;

/**
 * Repräsentiert den Standort eines Feldes vom Spieler.
 */
public class PlayersField {
    private int x;
    private int y;

    /**
     * Erstellt eine neue Repräsentation eines Standortes eines Feldes.
     *
     * @param x X-Koordinate des Feldes
     * @param y Y-Koordinate des Feldes
     */
    public PlayersField(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gibt die X-Koordinate zurück.
     *
     * @return X-Koordinate
     */
    public int getX() {
        return this.x;
    }

    /**
     * Gibt die Y-Koordinate zurück.
     *
     * @return Y-Koordinate
     */
    public int getY() {
        return this.y;
    }

    /**
     * Überprüft, ob ein anderes Feld aus der gleichen Position wie dieses liegt.
     *
     * @param other Das andere Feld.
     * @return true, wenn beide Felder auf der gleichen Position liegt, sonst false.
     */
    public boolean hasSamePosition(PlayersField other) {
        return this.x == other.getX() && this.y == other.getY();
    }

    /**
     * Überprüft, ob ein Feld benachbart zu diesem liegt. Wenn beide auf der
     * gleichen Position sind zählen sie nicht als benachbart.
     *
     * @param other Das andere Feld.
     * @return true, wenn die beiden Felder benachbart sind, sonst false.
     */
    public boolean isNeighbor(PlayersField other) {
        final int x = this.getX();
        final int y = this.getY();
        final int ox = other.getX();
        final int oy = other.getY();

        return (y - 1 == oy && x == ox) /* oben */ || (y + 1 == oy && x == ox) /* unten */
                || (x + 1 == ox && y == oy) /* rechts */ || (x - 1 == ox && y == oy) /* links */;
    }

    public boolean isNeighborOrEqual(PlayersField field) {
        return this.equals(field) || this.isNeighbor(field);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "PlayersField [x=" + this.x + ", y=" + this.y + "]";
    }
}
