package battleship.Game;

/**
 * Stellt einen Endzustand eines Spiele-Sitzung dar. Mit "erfolgreich" ist im
 * folgenden gemeint, dass der Status absichtlich vergeben wurden ist und nicht
 * beispielsweise dadurch, dass ein Verbindungsfehler aufgetreten ist.
 * Beispielsweise bedeutet `SUCCESSFUL_WON` nicht, dass der Spieler gewonnen
 * hat, weil die Verbindung unterbrochen wurden ist.
 */
public enum GameEndStatus {
    /**
     * Der Spieler hat erfolgreich gewonnen.
     */
    SUCCESSFUL_WON,

    /**
     * Der Spieler hat erfolgreich verloren.
     */
    SUCCESSFUL_LOST,

    /**
     * Der Spieler hat erfolgreich aufgegeben.
     */
    SUCCESSFUL_DRAW_FROM_PLAYER,

    /**
     * Der Gegner hat erfolgreich aufgegeben.
     */
    SUCCESSFUL_DRAW_FROM_PEER,

    /**
     * Die Verbindung wurde unterbrochen oder gestört.
     */
    CONNECTION_DISCONNECTED_OR_DISTURBED,

    /**
     * Der Spiel ist gar nicht erst zustande gekommen, weil die Vorbereitungen
     * (IAM-Paket) oder der Start (COIN-Paket) Fehlerhaft war.
     */
    GAME_PREPARATION_OR_START_FAILED
}
