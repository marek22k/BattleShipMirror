package battleship.playersThings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import battleship.ui.playgroundMatrix.PlaygroundMatrix;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Repräsentiert ein Spielfeld vom Spieler
 */
public class PlayersPlayingField {
    /**
     * Liste, welche die Schiffe des Spielers auf dem Spielfeld speichert.
     */
    private final List<PlayersShip> ships;

    /**
     * Größe des Spielfeldes.
     */
    private final int size;

    /**
     * Erstellt eine neue Repräsentation eines Spielfeldes.
     *
     * @param size Größe des Spielfeldes
     */
    public PlayersPlayingField(int size) {
        this.ships = new ArrayList<>();
        this.size = size;
    }

    /**
     * Überprüft, ob alle Schiffe versenkt wurden sind. Wenn ja, hat der Spieler
     * verloren.
     *
     * @return true, wenn alle Schiffe versenkt wurden sind, sonst false.
     */
    public boolean allSunk() {
        return this.ships.stream().allMatch(PlayersShip::isSunk);
    }

    /**
     * Gibt eine String-Repräsentation des Spielfeldes zurück. Es gibt eine
     * Auflistung aller Schiffe zurück.
     *
     * @return Die String-Repräsentation des Spielfeldes.
     */
    public String debugPrint() {
        final StringBuilder sb = new StringBuilder();
        for (final PlayersShip element : this.ships) {
            sb.append(element);
        }
        return sb.toString();
    }

    /**
     * Generiert und platziert zufällig Schiffe auf dem Spielfeld.
     *
     * @param list Eine Liste, welche die Größe der Schiffe beinhaltet. Beispiel:
     *             Eine Liste `[2, 5]` generiert zwei Schiffe, einmal zwei und
     *             einmal fünf Felder lang. Die Schiffe werden so platziert, dass
     *             sie nicht überlappend oder benachbart sind.
     */
    public void generateShips(Iterable<Integer> list) {
        final Random random = new Random();
        for (final Integer shipSize : list) {
            PlayersShip ship;
            do {
                ship = PlayersShip.generate(random, this.size, shipSize);
            } while (!this.isShipSuitable(ship));
            this.placeShip(ship);
        }
    }

    /**
     * Überprüft und verarbeitet einen Schuss auf das Spielfeld des Spielers. Es
     * überprüft, ob ein Schiff getroffen wurden ist. Ist dies der Fall, wird der
     * Treffer gespeichert.
     *
     * @param field Das Feld, welches angegriffen wurden ist.
     * @return null, wenn kein Schiff getroffen wurden ist, sonst das Schiff,
     *         welches getroffen wurden ist.
     */
    @Nullable
    public PlayersShip hit(PlayersField field) {
        for (final PlayersShip ship : this.ships) {
            if (ship.isOnField(field)) {
                ship.hit(field);
                return ship;
            }
        }
        return null;
    }

    /**
     * Überprüft, ob ein neues Schiff konfliktfrei auf dem aktuellen Spielfeld
     * platziert werden kann. Konfliktfrei meint damit, dass es mit keinem bereits
     * bestehenden Schiff überlappt oder benachbart ist.
     *
     * @param ship Das Schiff
     * @return true, wenn es konfliktfrei ist, sonst false.
     */
    public boolean isShipSuitable(PlayersShip ship) {
        for (final PlayersShip element : this.ships) {
            if (element.isNeighborOrEqual(ship)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Platziert ein Schiff. Das zu platzierende Schiff wird nicht auf
     * Konfliktfreiheit überprüft.
     *
     * @param ship
     */
    public void placeShip(PlayersShip ship) {
        this.ships.add(ship);
    }

    /**
     * Zeichnet das Spielfeld in ein graphisches Element in Form einer
     * `PlaygroundMatrix` ein.
     *
     * @param pm Die `PlaygroundMatrix`, in welche das Spielfeld eingezeichnet
     *           werden soll.
     */
    public void print(PlaygroundMatrix pm) {
        for (int y = 0; y < this.size; y++) {
            for (int x = 0; x < this.size; x++) {
                pm.setWater(x, y);
            }
        }

        for (final PlayersShip element : this.ships) {
            element.print(pm);
        }
    }
}
