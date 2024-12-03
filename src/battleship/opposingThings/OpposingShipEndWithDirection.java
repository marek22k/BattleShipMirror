package battleship.opposingThings;

/**
 * Repräsentiert ein Schiff des Gegners.
 */
public class OpposingShipEndWithDirection {
    /**
     * Die Lage des Schiffes
     */
    private final OpposingShipDirection direction;
    /**
     * Eines der Enden des Schiffes.
     */
    private final OpposingField end;

    /**
     * Erstellt eine Repräsentation eines Schiffes vom Feind.
     *
     * @param direction Lage des Schiffes
     * @param end       Ein Ende des Schiffes
     */
    public OpposingShipEndWithDirection(OpposingShipDirection direction, OpposingField end) {
        this.direction = direction;
        this.end = end;
    }

    /**
     * Gibt die Lage des Schiffes zurück.
     *
     * @return Die Lage des Schiffes.
     */
    public OpposingShipDirection getDirection() {
        return this.direction;
    }

    /**
     * Gibt das Feld zurück auf dem das Schiff liegt.
     *
     * @return Das Feld des Schiffes.
     */
    public OpposingField getEnd() {
        return this.end;
    }

    @Override
    public String toString() {
        return "OpposingShipEndWithDirection [direction=" + this.direction + ", end=" + this.end + "]";
    }
}
