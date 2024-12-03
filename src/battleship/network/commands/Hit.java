package battleship.network.commands;

import battleship.utils.Utils;

/**
 * Repräsentiert ein HIT-Paket
 */
public class Hit implements Command {
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
     * Der Status des übermittelten Feldes
     */
    private final HitStatus hitstatus;

    /**
     * Erstellt aus einem HIT-Paket in seiner genormten Übertragungsform als String
     * eine Repräsentation des Paketes.
     *
     * @param command Das HIT-Paket
     * @return Repräsentation des HIT-Pakets
     */
    public static Hit fromString(final String command) {
        final String coordinates = command.strip();
        final String xCoordinate = coordinates.substring(0, 1);
        if (xCoordinate.length() != 1) {
            throw new RuntimeException("Could not extract the X coordinate.");
        }
        final String yCoordinate = coordinates.substring(1, Utils.getFirstWordOrLine(coordinates).length());
        final String status = Utils.getStringAfterFirstSpace(coordinates);
        final HitStatus hitstatus;
        switch (status) {
            case "0":
                hitstatus = HitStatus.WATER;
                break;

            case "1":
                hitstatus = HitStatus.HIT;
                break;

            case "2":
                hitstatus = HitStatus.SUNK;
                break;

            case "3":
                hitstatus = HitStatus.SUNK_AND_VICTORY;
                break;

            default:
                hitstatus = HitStatus.UNKNOWN;
                break;
        }

        return new Hit(xCoordinate.charAt(0) - 'A', Integer.parseInt(yCoordinate) - 1, hitstatus);
    }

    /**
     * Erstellt eine Repräsentation des CHAT-Paketes.
     *
     * @param x         Die X-Koordinate, beginnend bei 0
     * @param y         Die Y-Koordinate, beginnend bei 0
     * @param hitstatus
     */
    public Hit(final int x, final int y, final HitStatus hitstatus) {
        this.x = x;
        this.y = y;
        this.hitstatus = hitstatus;
    }

    @Override
    public String getFullCommand() {
        final StringBuilder builder = new StringBuilder();
        builder.append("HIT ");

        builder.append((char) (this.x + 'A'));
        builder.append(this.y + 1);

        builder.append(' ');

        switch (this.hitstatus) {
            case WATER:
                builder.append('0');
                break;

            case HIT:
                builder.append('1');
                break;

            case SUNK:
                builder.append('2');
                break;

            case SUNK_AND_VICTORY:
                builder.append('3');
                break;

            default:
                builder.append("-1");
                break;
        }

        builder.append("\r\n");
        return builder.toString();
    }

    /**
     * Gibt den Status des Feldes zurück.
     *
     * @return Status des Feldes
     */
    public HitStatus getHitStatus() {
        return this.hitstatus;
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
        return this.x >= 0 && this.y >= 0 && this.hitstatus != HitStatus.UNKNOWN;
    }

}
