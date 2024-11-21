package battleship.network;

public interface EventHandler {
    public void handle(ConnectionEvent event, Object eventObject);
}
