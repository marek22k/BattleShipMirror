package battleship.ui.playgroundmatrix;

import java.io.Serializable;

/**
 * Listener, welcher aufgerufen wird, wenn auf ein Feld gefeuert wird.
 */
public interface FireListener extends Serializable {
    /**
     * Methode, welche beim feuern aufgerufen wird
     *
     * @param e Feuerevent, welches die Koordinaten des Feldes, auf was gefeuert
     *          wurde, enthält
     */
    void fire(FireEvent e);
}
