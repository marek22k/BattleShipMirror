package battleship.playersthings;

/**
 * Repräsentiert die Richtung eines Feldes.
 */
public enum PlayersShipDirection {
    /**
     * Das Schiff bedeckt nur ein Feld. Es hat daher keine Richtung.
     */
    SINGLE_FIELD,

    /**
     * von links nach rechts
     */
    HORIZONTAL,

    /**
     * von oben nach unten
     */
    VERTICAL
}
