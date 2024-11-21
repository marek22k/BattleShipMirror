package battleship.Game;

/**
 * Status, ob ein Server läuft.
 */
public enum ServerStatus {
    /**
     * Server läuft nicht. Er wurde beispielsweise gestoppt oder noch nie gestartet.
     */
    STOPPED,

    /**
     * Server läuft.
     */
    RUNNING
}
