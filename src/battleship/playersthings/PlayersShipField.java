package battleship.playersthings;

public class PlayersShipField extends PlayersField {
    private boolean sunk;

    public PlayersShipField(final int x, final int y) {
        super(x, y);
        this.sunk = false;
    }

    public void hit() {
        this.sunk = true;
    }

    public boolean isSunk() {
        return this.sunk;
    }

}
