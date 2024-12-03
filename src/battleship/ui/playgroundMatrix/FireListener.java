package battleship.ui.playgroundMatrix;

/**
 * Listener, welcher aufgerufen wird, wenn auf ein Feld gefeuert wird.
 */
public interface FireListener {
    /**
     * Methode, welche beim feuern aufgerufen wird
     *
     * @param e Feuerevent, welches die Koordinaten des Feldes, auf was gefeuert
     *          wurde, enthält
     */
    void fire(FireEvent e);
}
