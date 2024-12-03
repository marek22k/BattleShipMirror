package battleship.network;

public interface EventHandler {
    void handle(ConnectionEvent event, Object eventObject);
}
