package battleship.opposingThings;

public class OpposingField {
    private final int x;
    private final int y;

    public OpposingField(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public String toString() {
        return "OpposingField [x=" + this.x + ", y=" + this.y + "]";
    }
}
