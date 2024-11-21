package battleship.network.commands;

public class IAMU implements Command {
    private final String name;

    public static IAMU fromString(String command) {
        final String name = command.strip();
        return new IAMU(name);
    }

    public IAMU(String name) {
        this.name = name;
    }

    @Override
    public String getFullCommand() {
        final StringBuilder builder = new StringBuilder();
        builder.append("IAMU ");

        builder.append(this.name);

        builder.append("\r\n");
        return builder.toString();
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean isValid() {
        if (this.name.isBlank()) {
            return false;
        }

        return true;
    }
}
