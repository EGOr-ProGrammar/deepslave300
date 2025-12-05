package server;

import server.controller.ClientHandler;
import server.controller.GameLoop;
import server.controller.GameWorld;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TcpServer {
    private final ServerConfig config;
    private final GameWorld world;
    private final List<ClientHandler> clients = new ArrayList<>();
    private volatile boolean running = true;

    public TcpServer(ServerConfig config, GameWorld world) {
        this.config = config;
        this.world = world;
    }

    public void start() throws IOException {
        GameLoop gameLoop = new GameLoop(this, config.getTickRate());
        new Thread(gameLoop, "server-gameloop").start();

        try (ServerSocket serverSocket = new ServerSocket(config.getPort())) {
            System.out.println("✓ Server started on port " + config.getPort());
            System.out.println("✓ Waiting for players...\n");

            while (running) {
                try {
                    Socket socket = serverSocket.accept();

                    // Проверить лимит игроков
                    synchronized (clients) {
                        if (clients.size() >= config.getMaxPlayers()) {
                            System.out.println("✗ Connection rejected (max players): "
                                    + socket.getInetAddress());
                            socket.close();
                            continue;
                        }
                    }

                    System.out.println("✓ New client connected: " + socket.getInetAddress());

                    ClientHandler handler = new ClientHandler(socket, world);

                    synchronized (clients) {
                        clients.add(handler);
                    }

                    new Thread(handler::readLoop, "client-handler-" + clients.size()).start();

                } catch (IOException e) {
                    if (running) {
                        System.err.println("✗ Connection error: " + e.getMessage());
                    }
                }
            }
        }
    }

    public void broadcastState() {
        synchronized (clients) {
            // Удалить отключившихся
            clients.removeIf(ClientHandler::isDisconnected);

            // Рассылаем состояние каждому
            for (ClientHandler client : clients) {
                client.sendSnapshot(world.snapshot(client));
            }
        }
    }

    public void stop() {
        running = false;
        System.out.println("Server shutting down...");
    }

    public int getConnectedPlayers() {
        synchronized (clients) {
            return clients.size();
        }
    }

    public ServerConfig getConfig() {
        return config;
    }
}
