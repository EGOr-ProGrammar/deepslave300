package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {
    private final int port;
    private final GameWorld world;

    public TcpServer(int port, GameWorld world) {
        this.port = port;
        this.world = world;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Socket client = serverSocket.accept();
            ClientHandler handler = new ClientHandler(client, world);
            new Thread(handler::readLoop, "client-read").start();
            new Thread(() -> GameLoop.run(world, handler), "game-loop").start();
        }
    }
}

