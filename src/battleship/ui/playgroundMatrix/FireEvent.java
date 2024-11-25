package battleship.ui.playgroundMatrix;

public class FireEvent {
    private final int x;
    private final int y;

    public FireEvent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
}
