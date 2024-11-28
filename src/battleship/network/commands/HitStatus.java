package battleship.network.commands;

/**
 * Status, welcher in einem Hit-Paket übermittelt wird.
 */
public enum HitStatus {
    /**
     * Der Status ist unbekannt. Dieser Status kommt nicht in der
     * Netzwerkspezifikation vor. Er wird verwendet, wenn ein unbekannter Statuscode
     * verwendet wurden ist. Wenn das Paket mit diesem Statuscode in einen Befehl
     * als String umgewandelt wird, wird dieser Status durch den Code `-1`
     * repräsentiert.
     */
    UNKNOWN,

    /**
     * Es wurde das Wasser getroffen. Wird durch den Statuscode `0` repräsentiert.
     */
    WATER,

    /**
     * Es wurde ein Schiff getroffen, jedoch nicht versenkt. Wird durch den
     * Statuscode `1` repräsentiert.
     */
    HIT,

    /**
     * Es wurde ein Schiff getroffen und versenkt. Wird durch den Statuscode `2`
     * repräsentiert.
     */
    SUNK,

    /**
     * Es wurde das letzte Schiff getroffen und dabei versenkt. Der Spieler, welcher
     * den Zug ausgeführt hat, hat damit gewonnen. Wird durch den Statuscode `3`
     * repräsentiert.
     */
    SUNK_AND_VICTORY
}
