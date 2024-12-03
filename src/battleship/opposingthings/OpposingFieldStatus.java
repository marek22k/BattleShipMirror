package battleship.opposingthings;

/**
 * Repräsentiert den Inhalt (Status) eines Feldes vom Gegner
 */
public enum OpposingFieldStatus {
    /**
     * Unbekannt. Es ist nicht bekannt, was sich auf dem Feld des Gegners verbirgt.
     */
    UNKNOWN,

    /**
     * Wasser. Auf dem Feld des Gegners verbirgt sich Wasser.
     */
    WATER,

    /**
     * Schiff. Auf dem Feld des Gegners verbirgt sich ein Teil eines Schiff, welches
     * noch nicht untergegangen bzw. versenkt wurden ist.
     */
    SHIP,

    /**
     * Versunkenes Schiff. Auf dem Feld des Gegners verbirgt sich ein Teil eines
     * Schiff, welches versenkt wurden ist.
     */
    SUNK /* Versunkendes Schiff */
}
