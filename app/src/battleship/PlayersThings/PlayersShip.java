package battleship.PlayersThings;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;

import battleship.ui.PlaygroundMatrix.PlaygroundMatrix;

public class PlayersShip {
    private final ArrayList<PlayersShipField> fields;
    private final PlayersShipDirection direction;
    private final int length;

    public static PlayersShip generate(Random random, int size, int length) {
        if (length >= size) {
            throw new RuntimeException(
                    "It is not possible to create a ship without bulges that is larger than the height or width of the playing field."
            );
        }

        final int max = size - length;
        final int startx = random.nextInt(max);
        final int starty = random.nextInt(max);
        PlayersShipDirection direction;

        if (length == 1) {
            direction = PlayersShipDirection.SINGLE_FIELD;
        } else {
            final boolean isHorizontal = random.nextBoolean();
            direction = isHorizontal ? PlayersShipDirection.HORIZONTAL : PlayersShipDirection.VERTICAL;
        }

        return new PlayersShip(new PlayersField(startx, starty), direction, length);
    }

    public PlayersShip(PlayersField start, PlayersShipDirection direction, int length) {
        this.direction = direction;
        this.length = length;
        this.fields = new ArrayList<>();

        if (this.length < 1) {
            throw new IllegalArgumentException("There can be no ship that has a length of 0.");
        }

        if (this.direction == PlayersShipDirection.SINGLE_FIELD && this.length != 1) {
            throw new IllegalArgumentException(
                    "There cannot be a ship that covers a field but covers more than one field."
            );
        }

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

    public boolean isNeighborOrEqual(PlayersShip other) {
        // for (int i = 0; i < this.fields.size(); i++) {
        // for (int j = 0; j < other.fields.size(); j++) {
        // if (this.fields.get(i).isNeighborOrEqual(other.fields.get(j))) {
        // return true;
        // }
        // }
        // }
        //
        // return false;
        return this.fields.stream().anyMatch(f1 -> other.fields.stream().anyMatch(f1::isNeighborOrEqual));
    }

    public boolean isOnField(PlayersField field) {
        return this.fields.stream().anyMatch(f -> f.hasSamePosition(field));
    }

    public boolean isSunk() {
        return this.fields.stream().allMatch(PlayersShipField::isSunk);
    }

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
