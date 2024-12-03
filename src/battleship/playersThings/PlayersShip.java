package battleship.playersThings;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import battleship.ui.playgroundMatrix.PlaygroundMatrix;

/**
 * Repräsentiert ein Schiff eines Spielers.
 */
public class PlayersShip {
    /**
     * Speichert die Felder des Schiffes und ob diese getroffen wurden sind.
     */
    private final List<PlayersShipField> fields;

    /**
     * Speichert die Richtung des Schiffes
     */
    private final PlayersShipDirection direction;

    /**
     * Speichert die Länge des Schiffes
     */
    private final int length;

    /**
     * Generiert ein neues zufälliges Schiff.
     * 
     * @param random Random-Objekt, welches zur Generierung verwendet werden soll.
     * @param size   Größe des Spielfeldes, auf welches das Schiff passen soll. Es
     *               wird angenommen, dass das Spielfeld quadratisch ist.
     * @param length Die Länge des Schiffes.
     * @return Das generierte Schiff.
     */
    public static PlayersShip generate(Random random, int size, int length) {
        /* Es kann kein Schiff generiert werden, welches größer als das Spielfeld ist */
        if (length >= size) {
            throw new RuntimeException(
                    "It is not possible to create a ship without bulges that is larger than the height or width of the playing field."
            );
        }

        /*
         * Maximale Länge des Schiffes ohne das es über den Spielfeldrand hinaus geht.
         */
        final int max = size - length;
        /* Startposition des Schiffes */
        final int startx = random.nextInt(max);
        final int starty = random.nextInt(max);
        /* Richtung des Schiffes */
        PlayersShipDirection direction;

        if (length == 1) {
            direction = PlayersShipDirection.SINGLE_FIELD;
        } else {
            final boolean isHorizontal = random.nextBoolean();
            direction = isHorizontal ? PlayersShipDirection.HORIZONTAL : PlayersShipDirection.VERTICAL;
        }

        return new PlayersShip(new PlayersField(startx, starty), direction, length);
    }

    /**
     * Erstellt eine neue Repräsentation des Schiffes vom Spieler
     * 
     * @param start     Start des Schiffes
     * @param direction Richtung des Schiffes
     * @param length    Länge des Schiffes
     */
    public PlayersShip(PlayersField start, PlayersShipDirection direction, int length) {
        this.direction = direction;
        this.length = length;
        this.fields = new ArrayList<>();

        /* Es kann kein Schiff geben, welches weniger als ein Feld einnimmt. */
        if (this.length < 1) {
            throw new IllegalArgumentException("There can be no ship that has a length of 0.");
        }

        /*
         * Es kann kein Schiff geben, welches ein Feld groß ist, aber größer als ein
         * Feld ist.
         */
        if (this.direction == PlayersShipDirection.SINGLE_FIELD && this.length != 1) {
            throw new IllegalArgumentException(
                    "There cannot be a ship that covers a field but covers more than one field."
            );
        }

        /* Generiere die eigentlichen Schiffsfelder */
        switch (this.direction) {
            case HORIZONTAL:
                for (int i = 0; i < length; i++) {
                    final int x = start.getX() + i;
                    final int y = start.getY();
                    this.fields.add(new PlayersShipField(x, y));
                }
                break;

            case VERTICAL:
                for (int i = 0; i < length; i++) {
                    final int x = start.getX();
                    final int y = start.getY() + i;
                    this.fields.add(new PlayersShipField(x, y));
                }
                break;

            case SINGLE_FIELD:
                this.fields.add(new PlayersShipField(start.getX(), start.getY()));
                break;
        }
    }

    /**
     * Gibt die größte X-Koordinate der Schifffelder zurück.
     * 
     * @return Die größte X-Koordinate.
     */
    public int getMaxX() {
        return this.fields.stream().mapToInt(PlayersShipField::getX).max().orElseThrow(NoSuchElementException::new); // Wird
                                                                                                                     // nie
                                                                                                                     // gewurfen,
                                                                                                                     // da
                                                                                                                     // es
                                                                                                                     // mindentes
                                                                                                                     // ein
                                                                                                                     // Element
                                                                                                                     // geben
                                                                                                                     // muss.
    }

    /**
     * Gibt die größte Y-Koordinate der Schifffelder zurück.
     * 
     * @return Die größte Y-Koordinate.
     */
    public int getMaxY() {
        return this.fields.stream().mapToInt(PlayersShipField::getY).max().orElseThrow(NoSuchElementException::new); // Wird
                                                                                                                     // nie
                                                                                                                     // gewurfen,
                                                                                                                     // da
                                                                                                                     // es
                                                                                                                     // mindentes
                                                                                                                     // ein
                                                                                                                     // Element
                                                                                                                     // geben
                                                                                                                     // muss.
    }

    /**
     * Markiert ein Schiffsfeld als getroffen.
     * 
     * @param field Das Schiffsfeld, welches als getroffen markiert werden soll.
     */
    public void hit(PlayersField field) {
        this.fields.stream().filter(f -> f.hasSamePosition(field)).findFirst().ifPresent(PlayersShipField::hit);
    }

    /**
     * Gibt true zurück, wenn das Schiff ein einem quadratischen Spielfeld von der
     * Größe size liegen.
     *
     * @param size
     * @return
     */
    public boolean isInField(int size) {
        return this.getMaxX() < size && this.getMaxY() < size;
    }

    /**
     * Überprüft, ob ein Schiff benachbart oder an derselben Position wie das
     * Aktuelle ist.
     * 
     * @param other Das andere Schiff
     * @return true, wenn es benachbart oder an der gleichen Position ist.
     */
    public boolean isNeighborOrEqual(PlayersShip other) {
        return this.fields.stream().anyMatch(f -> other.fields.stream().anyMatch(f::isNeighborOrEqual));
    }

    /**
     * Überprüft, ob das Schiff ein bestimmtes Spielfeld abdeckt.
     * 
     * @param field Das Spielfeld.
     * @return true, wenn das Schiff das Spielfeld abdeckt, sonst false.
     */
    public boolean isOnField(PlayersField field) {
        return this.fields.stream().anyMatch(f -> f.hasSamePosition(field));
    }

    /**
     * Überprüft, ob das Schiff versunken ist.
     * 
     * @return true, wenn das Schiff versunken ist, sonst false.
     */
    public boolean isSunk() {
        return this.fields.stream().allMatch(PlayersShipField::isSunk);
    }

    /**
     * Zeichnet das Schiff in ein graphisches Element in Form einer
     * `PlaygroundMatrix` ein.
     * 
     * @param pm Die `PlaygroundMatrix`, auf welche das Schiff eingezeichnet werden
     *           soll.
     */
    public void print(PlaygroundMatrix pm) {
        for (final PlayersShipField element : this.fields) {
            final int x = element.getX();
            final int y = element.getY();
            final boolean sunk = element.isSunk();
            if (sunk) {
                pm.setSunk(x, y);
            } else {
                pm.setShip(x, y);
            }
        }
    }

    @Override
    public String toString() {
        return "PlayersShip [start=" + this.fields.getFirst() + ", direction=" + this.direction + ", length="
                + this.length + "]";
    }
}
