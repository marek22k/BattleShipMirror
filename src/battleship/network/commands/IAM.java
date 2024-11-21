package battleship.network.commands;

import battleship.Constants;
import battleship.utils.Utils;

public class IAM implements Command {
    private final String name;
    private final String level;

    public static IAM fromString(String command) {
        final String level = Utils.getFirstWordOrLine(command);
        final String name = Utils.getStringAfterFirstSpace(command);
        return new IAM(name, level);
    }

    public IAM(String name, String level) {
        this.name = name;
        this.level = level;
    }

    @Override
    public String getFullCommand() {
        final StringBuilder builder = new StringBuilder();
        builder.append("IAM ");

        builder.append(this.level);
        builder.append(' ');

        builder.append(this.name);

        builder.append("\r\n");
        return builder.toString();
    }

    public String getLevel() {
        return this.level;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean isValid() {
        if (this.name.isBlank() || this.level.isBlank() || this.name.length() > 32) {
            return false;
        }

        try {
            final int level = Integer.parseInt(this.level);
            if (level > Constants.NUMBER_OF_LEVELS) {
                return false;
            }
        } catch (final Exception e) {
            return false;
        }

        return true;
    }
}
