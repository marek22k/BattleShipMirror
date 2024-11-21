package battleship.network.commands;

public class Chat implements Command {
    private final String message;

    public static Chat fromString(String command) {
        return new Chat(command.strip());
    }

    public Chat(String message) {
        this.message = message;
    }

    @Override
    public String getFullCommand() {
        final StringBuilder builder = new StringBuilder();
        builder.append("CHAT ");

        builder.append(this.message);

        builder.append("\r\n");
        return builder.toString();
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
