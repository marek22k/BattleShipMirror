package battleship.network.commands;

import battleship.utils.Utils;

public class Hit implements Command {
    /*
     * Koordination werden beginnend bei 0 gespeichert, jedoch beginnend bei 1
     * übertragen.
     */
    private final int x;
    private final int y;
    private final HitStatus hitstatus;

    public static Hit fromString(String command) {
        final String coordinates = command.strip();
        final String xCoordinate = coordinates.substring(0, 1);
        if (xCoordinate.length() != 1) {
            throw new RuntimeException("Could not extract the X coordinate.");
        }
        final String yCoordinate = coordinates.substring(1, Utils.getFirstWordOrLine(coordinates).length());
        final String status = Utils.getStringAfterFirstSpace(coordinates);
        HitStatus hitstatus;
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

    public Hit(int x, int y, HitStatus hitstatus) {
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

    public HitStatus getHitStatus() {
        return this.hitstatus;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public boolean isValid() {
        return this.x >= 0 && this.y >= 0 && this.hitstatus != HitStatus.UNKNOWN;
    }

}
