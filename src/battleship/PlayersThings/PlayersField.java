package battleship.PlayersThings;

public class PlayersField {
    private int x;
    private int y;

    public PlayersField(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public boolean hasSamePosition(PlayersField other) {
        return this.x == other.getX() && this.y == other.getY();
    }

    public boolean isNeighbor(PlayersField other) {
        final int x = this.getX();
        final int y = this.getY();
        final int ox = other.getX();
        final int oy = other.getY();

        /* oben */
        if (y - 1 >= 0 && x == ox && y - 1 == oy) {
            return true;
        }
        /* rechts */
        /* rechts */
        if (x + 1 == ox && y == oy || x == ox && y + 1 == oy) {
            return true;
        }
        /* links */
        if (x - 1 >= 0 && x - 1 == ox && y == oy) {
            return true;
        }

        return false;
    }

    public boolean isNeighborOrEqual(PlayersField field) {
        return this.equals(field) || this.isNeighbor(field);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "PlayersField [x=" + this.x + ", y=" + this.y + "]";
    }
}
