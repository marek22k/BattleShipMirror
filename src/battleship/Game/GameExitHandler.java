package battleship.Game;

/**
 * Interface für eine Funktion, welche beim Beenden einer Spiele-Sitzung
 * aufgerufen werden kann.
 */
public interface GameExitHandler {
    /**
     * Funktion, welche einen benutzerdefinierte Aufgabe ausführt nachdem eine
     * Spiele-Sitzung beendet wurden ist
     *
     * @param status Status, mit welchem das Spiel beendet wurden ist
     */
    void handle(GameEndStatus status);
}
