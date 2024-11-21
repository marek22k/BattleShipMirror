package battleship.PlayersThings;

public class PlayersShipField extends PlayersField {
    private boolean sunk;

    public PlayersShipField(int x, int y) {
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
