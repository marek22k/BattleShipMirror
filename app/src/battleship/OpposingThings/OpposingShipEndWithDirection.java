package battleship.OpposingThings;

public class OpposingShipEndWithDirection {
    private final OpposingShipDirection direction;
    private final OpposingField end;

    public OpposingShipEndWithDirection(OpposingShipDirection direction, OpposingField end) {
        this.direction = direction;
        this.end = end;
    }

    public OpposingShipDirection getDirection() {
        return this.direction;
    }

    public OpposingField getEnd() {
        return this.end;
    }

    @Override
    public String toString() {
        return "OpposingShipEndWithDirection [direction=" + this.direction + ", end=" + this.end + "]";
    }
}
