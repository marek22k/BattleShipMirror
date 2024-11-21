package battleship.network.commands;

public class Shoot implements Command {
    /*
     * Koordination werden beginnend bei 0 gespeichert, jedoch beginnend bei 1
     * übertragen.
     */
    private final int x;
    private final int y;

    public static Shoot fromString(String command) {
        final String coordinates = command.strip();
        final String xCoordinate = coordinates.substring(0, 1);
        if (xCoordinate.length() != 1) {
            throw new RuntimeException("Could not extract the X coordinate.");
        }

        final String yCoordinate = coordinates.substring(1);
        return new Shoot(xCoordinate.charAt(0) - 'A', Integer.parseInt(yCoordinate) - 1);
    }

    public Shoot(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String getFullCommand() {
        final StringBuilder builder = new StringBuilder();
        builder.append("SHOOT ");

        builder.append((char) (this.x + 'A'));
        builder.append(this.y + 1);

        builder.append("\r\n");
        return builder.toString();
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public boolean isValid() {
        return this.x >= 0 && this.y >= 0;
    }

}
