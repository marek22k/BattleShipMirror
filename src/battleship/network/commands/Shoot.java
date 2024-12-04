package battleship.network.commands;

/**
 * Repräsentiert ein SHOOT-Paket
 */
public class Shoot implements Command {
    /**
     * X-Koordinate, gespeichert als Zahl beginnend mit 0, übertragen als Buchstabe
     * beginnend mit A
     */
    private final int x;
    /**
     * Y-Koordinate, gespeichert als Zahl beginnend mit 0, übertragen als Zahl
     * beginnend mit 1
     */
    private final int y;

    /**
     * Erstellt aus einem SHOOT-Paket in seiner genormten Übertragungsform als
     * String
     * eine Repräsentation des Paketes.
     *
     * @param command Das SHOOT-Paket
     * @return Repräsentation des SHOOT-Pakets
     */
    public static Shoot fromString(final String command) {
        final String coordinates = command.strip();
        final String xCoordinate = coordinates.substring(0, 1);
        if (xCoordinate.length() != 1) {
            throw new RuntimeException("Could not extract the X coordinate.");
        }

        final String yCoordinate = coordinates.substring(1);
        return new Shoot(xCoordinate.charAt(0) - 'A', Integer.parseInt(yCoordinate) - 1);
    }

    /**
     * Erstellt eine Repräsentation des SHOOT-Paketes.
     *
     * @param x Die X-Koordinate, beginnend bei 0
     * @param y Die Y-Koordinate, beginnend bei 0
     */
    public Shoot(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String getFullCommand() {
        final StringBuilder builder = new StringBuilder();
        builder.append("SHOOT ").append((char) (this.x + 'A')).append(this.y + 1).append("\r\n");

        return builder.toString();
    }

    /**
     * Gibt die X-Koordinate zurück.
     *
     * @return X-Koordinate, beginnend bei 0.
     */
    public int getX() {
        return this.x;
    }

    /**
     * Gibt die Y-Koordinate zurück.
     *
     * @return Y-Koordinate, beginnend bei 0.
     */
    public int getY() {
        return this.y;
    }

    @Override
    public boolean isValid() {
        return this.x >= 0 && this.y >= 0;
    }

    @Override
    public String toString() {
        return "Shoot [x=" + this.x + ", y=" + this.y + "]";
    }
}
