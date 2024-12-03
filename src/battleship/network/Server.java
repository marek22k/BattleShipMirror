package battleship.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import battleship.Constants;

public class Server {
    private ServerSocket serversocket;

    public Server() throws IOException {
        this(Constants.SERVER_PORT);
    }

    public Server(final int port) throws IOException {
        this.serversocket = new ServerSocket(port, 50, null);
    }

    public void stop() throws IOException {
        this.serversocket.close();
        this.serversocket = null;
    }

    public Socket waitForClient() throws IOException {
        return this.serversocket.accept();
    }
}
